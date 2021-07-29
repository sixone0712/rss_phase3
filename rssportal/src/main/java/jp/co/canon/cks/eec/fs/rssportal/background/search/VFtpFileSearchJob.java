package jp.co.canon.cks.eec.fs.rssportal.background.search;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jp.co.canon.ckbs.eec.fs.collect.controller.param.VFtpSssListRequestResponse;
import jp.co.canon.ckbs.eec.fs.collect.model.FtpRequest;
import jp.co.canon.ckbs.eec.fs.collect.model.VFtpSssListRequest;
import jp.co.canon.ckbs.eec.fs.collect.service.VFtpFileInfo;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnector;
import jp.co.canon.cks.eec.fs.rssportal.model.vftp.VFtpFileInfoExtends;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class VFtpFileSearchJob extends FileSearchJob {

    private String command;

    List<VFtpFileInfoExtends> searchFiles;

    public VFtpFileSearchJob(
            FileServiceManageConnector connector,
            String jobId,
            String[] fabNames,
            String[] machineNames,
            String command,
            String root,
            int maxThreads
    ) throws ParseException {

        super(connector, jobId, fabNames, machineNames, root, maxThreads);

        this.jobType = FileSearchManager.VFTP_SSS_TYPE;
        this.command = command;
        this.searchFiles = new ArrayList<>();
    }

    @Override
    protected void distribute() {
        for(int i=0; i<machineNames.length;++i) {
            Process process = new Process(fabNames[i], machineNames[i], command);
            submitProcess(process);
        }
    }

    @Override
    protected void harvest() {
        if(!canceling) {
            for (Future future : futures) {
                try {
                    searchFiles.addAll((List) future.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void printout() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.createObjectNode();
        ArrayNode fileArrayNode = mapper.createArrayNode();
        if(!canceling) {
            for (VFtpFileInfoExtends file : searchFiles) {
                JsonNode fileNode = mapper.convertValue(file, JsonNode.class);
                fileArrayNode.add(fileNode);
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        ObjectNode objectNode = (ObjectNode)rootNode;
        objectNode.put("jobId", jobId)
                .put("requestTime", format.format(requestTime.getTime()))
                .put("searchedCount", searchedCount.get())
                .put("status", canceling?FileSearchManager.CANCELED:status)
                .put("finishTime", format.format(finishTime.getTime()))
                .put("operatingMillis", operatingMillis)
                .put("finishes", finishes.get())
                .put("jobType", FileSearchManager.FTP_TYPE);
        objectNode.set("files", fileArrayNode);

        File file = Paths.get(root, jobId+".data").toFile();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        try {
            writer.writeValue(file, rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List getSearchFiles() {
        return searchFiles;
    }

    private class Process implements Callable<List<VFtpFileInfoExtends>> {

        String fab;
        String machine;
        String command;
        String status;
        String prefix;
        List<VFtpFileInfoExtends> files;

        public Process(String fab, String machine, String command) {
            this.fab = fab;
            this.machine = machine;
            this.command = command;
            this.status = FileSearchManager.IN_PROGRESS;
            this.prefix = String.format("(%s|%s) %s ", fab, machine, command);
            this.files = new ArrayList<>();
        }

        public void getFileList() {
            logger.info(prefix+"start");

            long startMillis = System.currentTimeMillis();

            VFtpSssListRequestResponse response = connector.createVFtpSssListRequest(machine, command);
            if(response.getErrorMessage()!=null) {
                logger.error(prefix+"failed to create search request with connector");
                return;
            }

            try {
                Thread.sleep(10);
                String requestNo = response.getRequest().getRequestNo();

                while(true) {

                    response = connector.getVFtpSssListRequest(machine, requestNo);
                    FtpRequest.Status sts = response.getRequest().getStatus();

                    if(sts==FtpRequest.Status.ERROR) {
                        status = FileSearchManager.ERROR;
                        logger.info(prefix+"error");
                        break;
                    } else if(sts==FtpRequest.Status.CANCEL) {
                        status = FileSearchManager.DONE;
                        logger.info(prefix+"canceled");
                        break;
                    } else if(sts==FtpRequest.Status.EXECUTED) {
                        for(VFtpFileInfo file: response.getRequest().getFileList()) {
                            VFtpFileInfoExtends f = new VFtpFileInfoExtends();
                            f.setFabName(fab);
                            f.setMachineName(machine);
                            f.setCommand(command);
                            f.setFileName(file.getFileName());
                            f.setFileType(file.getFileType());
                            f.setFileSize(file.getFileSize());

                            files.add(f);
                        }
                        break;
                    }
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                logger.info(prefix+"interrupted. cancel searching");
                files.clear();
                status = FileSearchManager.DONE;
            }
            logger.info(prefix+"done. "+files.size()+" files "+(System.currentTimeMillis()-startMillis)+" ms");
        }

        @Override
        public List<VFtpFileInfoExtends> call() throws Exception {
            getFileList();
            searchedCount.addAndGet(files.size());
            finishes.incrementAndGet();
            return files;
        }
    }

}
