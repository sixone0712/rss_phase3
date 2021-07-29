package jp.co.canon.cks.eec.fs.rssportal.background.fileserviceproc;

import jp.co.canon.cks.eec.fs.portal.bussiness.CustomURL;
import jp.co.canon.cks.eec.fs.rssportal.background.FileDownloadContext;
import jp.co.canon.cks.eec.fs.rssportal.background.FtpWorker;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileDownloadServiceProc extends Thread {

    public enum Status { InProgress, Finished, Canceled, Error, Timeout }
    private final EspLog log = new EspLog(getClass());

    @FunctionalInterface
    private interface Processable {
        void run() throws FileDownloadServiceException;
    }

    @Getter
    private final FileDownloadContext context;
    private final FileDownloadHandler handler;
    private final FileDownloadServiceCallback callback;

    private Status status;
    private List<Processable> pipes;

    private String _name;

    public FileDownloadServiceProc(FileDownloadHandler handler,
                                   FileDownloadContext context,
                                   FileDownloadServiceCallback callback) {
        this.handler = handler;
        this.context = context;
        this.callback = callback;
        this.status = Status.InProgress;
        this.start();
    }

    private void buildPipes() {
        pipes = new ArrayList<>();
        pipes.add(this::register);
        pipes.add(this::download);
        pipes.add(this::transfer);
        if(context.isAchieveDecompress()) {
            pipes.add(this::decompress);
        }
    }

    @Override
    public void run() {
        buildPipes();
        log.info(getProcessName()+" download pipe start");
        for(Processable pipe: pipes) {
            try {
                pipe.run();
            } catch (FileDownloadServiceException e) {
                FileDownloadServiceException.Error error = e.getError();
                if(error==FileDownloadServiceException.Error.timeout) {
                    status = Status.Timeout;
                } else {
                    status = Status.Error;
                }
                log.error("stop running (cause="+status.name()+")");
                callback.call(this);
                return;
            }
        }
        status = Status.Finished;
        callback.call(this);
    }

    private void register() throws FileDownloadServiceException {
        log.info("["+getProcessName()+"#register] register()");
        String requestNo = handler.createDownloadRequest();
        if(requestNo==null) {
            log.error("["+getProcessName()+"#register] faield to create vftp(compat) request");
            //return;
            throw new FileDownloadServiceException(FileDownloadServiceException.Error.error);
        }
        context.setRequestNo(requestNo);
        log.info("["+getProcessName()+"#register] request-no="+requestNo);
    }

    private void download() throws FileDownloadServiceException {
        log.info("["+getProcessName()+"#download] download  machine="+context.getTool()+" category="+context.getLogType());

        long updateTime = System.currentTimeMillis();
        FileDownloadInfo lastInfo = null;

        int loop = 0;
        while(true) {
            FileDownloadInfo info = handler.getDownloadedFiles();
            if(info==null || info.isError()) {
                log.error("["+getProcessName()+"#download] download error occurs");
                throw new FileDownloadServiceException(FileDownloadServiceException.Error.error);
            }
            if(++loop==10) {
                log.info(String.format("req=%s %d/%d %s", context.getRequestNo(), info.getDownloadFiles(),
                        info.getRequestFiles(), info.isFinish()));
                loop = 0;
            }
            long current = System.currentTimeMillis();
            if(lastInfo==null || info.getDownloadBytes()==0 || lastInfo.getDownloadBytes()<info.getDownloadBytes()) {
                lastInfo = info;
                updateTime = current;
            } else if((updateTime+300000)<current){
                log.info("["+getProcessName()+"#download] update="+updateTime+" current="+current);
                throw new FileDownloadServiceException(FileDownloadServiceException.Error.timeout);
            }
            context.setDownloadInfo(info);
            if(info.isFinish()) {
                log.info("["+getProcessName()+"#download] download completed");
                break;
            }
            try {
                sleep(500);
            } catch (InterruptedException e) {
                log.info("["+getProcessName()+"#download] downloading interrupt occurs");
                handler.cancelDownloadRequest();
                throw new FileDownloadServiceException(FileDownloadServiceException.Error.error);
            }
        }
    }

    private void transfer() throws FileDownloadServiceException {
        log.info("["+getProcessName()+"#transfer] transfer()");

        File dir = new File(context.getOutPath());
        if(!dir.exists()) {
            dir.mkdirs();
        }

        String downloadFilename;
        if(context.isAchieveDecompress()) {
            downloadFilename = Paths.get(context.getOutPath(), context.getCommand()+".zip").toString();
        } else {
            downloadFilename = Paths.get(context.getOutPath(), handler.getOutputFileName(context)).toString();
        }
        log.info("request filename="+downloadFilename);
        String achieve = handler.downloadFile(downloadFilename);
        log.info("["+getProcessName()+"#transfer] download complete. "+achieve);
        context.setLocalFilePath(achieve);
    }

    private void decompress() throws FileDownloadServiceException {
        log.info("["+getProcessName()+"#decompress] decompress (achieve="+context.getLocalFilePath()+")");
        if (!context.getLocalFilePath().endsWith(".zip")) {
            log.error("no achieve file");
            throw new FileDownloadServiceException(FileDownloadServiceException.Error.error);
        }

        File zip = new File(context.getLocalFilePath());
        if(!zip.exists() || zip.isDirectory()) {
            log.error("["+getProcessName()+"#decompress] wrong achieve file type  "+zip.toString());
            throw new FileDownloadServiceException(FileDownloadServiceException.Error.error);
        }

        Path path;
        String sub = context.getSubDir();
        if(sub!=null && !sub.isEmpty()) {
            path = Paths.get(zip.getParent(), sub);
        } else {
            path = Paths.get(zip.getParent());
        }
        File outDir = path.toFile();
        if(!outDir.exists()) {
            outDir.mkdirs();
        }

        byte[] buf = new byte[1024*64];
        int size;
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))) {
            ZipEntry entry = zis.getNextEntry();
            while(entry!=null) {
                File tmpFile = new File(path.toString(), entry.getName());
                try(FileOutputStream fos = new FileOutputStream(tmpFile)) {
                    while((size=zis.read(buf))>0) {
                        fos.write(buf, 0, size);
                    }
                }
                entry = zis.getNextEntry();
            }
        } catch (IOException e) {
            log.error("["+getProcessName()+"#decompress] extraction failed");
            throw new FileDownloadServiceException(FileDownloadServiceException.Error.error);
        }
        zip.delete();
    }

    private String getProcessName() {
        if(_name==null) {
            if (context == null)
                return "null";

            StringBuilder sb = new StringBuilder(context.getTool());
            sb.append(":");
            switch (context.getFtpType()) {
                case "ftp":
                    sb.append(context.getLogType());
                    break;
                case "vftp_compat":
                    sb.append(context.getCommand());
                    break;
                case "vftp_sss":
                    sb.append(context.getDirectory());
                    break;
                default:
                    sb.append("undef");
            }
            _name = sb.toString();
        }
        return _name;
    }

    public Status getStatus() {
        return status;
    }
}
