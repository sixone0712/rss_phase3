package jp.co.canon.cks.eec.fs.rssportal.background.search;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jp.co.canon.ckbs.eec.fs.collect.service.FileInfo;
import jp.co.canon.ckbs.eec.fs.collect.service.LogFileList;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnector;
import jp.co.canon.cks.eec.fs.rssportal.common.Tool;
import jp.co.canon.cks.eec.fs.rssportal.model.ftp.RSSFtpSearchResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class FtpFileSearchJob extends FileSearchJob {

    private String[]    categoryNames;
    private String[]    categoryCodes;
    private String      start;
    private String      end;

    private Date    startDate;
    private Date    endDate;
    private int     depthLimit;
    private SimpleDateFormat dateFormat;

    private List<RSSFtpSearchResponse> searchFiles;
    private List<Report> reports;

    public FtpFileSearchJob(
            FileServiceManageConnector connector,
            String jobId,
            String[] fabNames,
            String[] machineNames,
            String[] categoryNames,
            String[] categoryCodes,
            String start,
            String end,
            String root,
            int maxThreads,
            int depthLimit
    ) throws ParseException {

        super(connector, jobId, fabNames, machineNames, root, maxThreads);

        this.jobType = FileSearchManager.FTP_TYPE;
        this.categoryNames = categoryNames;
        this.categoryCodes = categoryCodes;
        this.start = start;
        this.end = end;

        this.dateFormat = Tool.getSimpleDateFormat();
        this.startDate = dateFormat.parse(start);
        this.endDate = dateFormat.parse(end);
        this.depthLimit = depthLimit;

        this.searchFiles = new ArrayList<>();
        reports = new ArrayList<>();

        _printJobBrief();
    }

    @Override
    protected void distribute() {
        for(int i=0; i<machineNames.length; ++i) {
            for(int j=0; j<categoryNames.length; ++j) {
                JobContext job = new JobContext(fabNames[i], machineNames[i], categoryNames[j], categoryCodes[j],
                        start, end,null, null, 0);
                logger.info(String.format("add job (%s/%s/%s)", job.machine, job.categoryName, job.dir));
                submitProcess(job);
            }
        }
    }

    @Override
    protected void harvest() {
        if(!canceling) {
            for (Future future : futures) {
                try {
                    List<RSSFtpSearchResponse> _files = (List<RSSFtpSearchResponse>) future.get();
                    searchFiles.addAll(_files);
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
            for (RSSFtpSearchResponse file : searchFiles) {
                JsonNode fileNode = mapper.convertValue(file, JsonNode.class);
                fileArrayNode.add(fileNode);
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        ObjectNode objectNode = (ObjectNode)rootNode;
        objectNode.put("jobId", jobId)
                .put("requestTime", format.format(requestTime.getTime()))
                .put("start", start)
                .put("end", end)
                .put("searchedCount", searchedCount.get())
                .put("status", canceling?FileSearchManager.CANCELED:status)
                .put("finishTime", format.format(finishTime.getTime()))
                .put("operatingMillis", operatingMillis)
                .put("finishes", finishes.get())
                .put("jobType", FileSearchManager.FTP_TYPE);
        objectNode.set("files", fileArrayNode);

        File file = Paths.get(root, this.jobId+".data").toFile();
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

    @Override
    protected void shutdownAction() {
        _printJobReport();
    }

    private void printSearchFiles() {
        for(RSSFtpSearchResponse file: searchFiles) {
            try {
                logger.info(String.format("> %s\t%s\t%s\t%s\t%s\t%d",
                        file.getMachineName(), file.getCategoryName(), file.getFileName(), file.getFilePath(),
                        file.getFileDate(), file.getFileSize()));
            } catch (RuntimeException e) {
                logger.error("printSearchFiles error on printing "+file.toString());
            }
        }
    }

    private void pushJob(JobContext job) {
        works.incrementAndGet();
        logger.info(String.format("add job (%s/%s/%s)", job.machine, job.categoryName, job.dir));
    }

    private boolean isDirectory(FileInfo file) {
        return file.getType().equals("D");
    }

    private void _printJobBrief() {
        logger.info("Machines");
        for(String machine: machineNames) {
            logger.info("  "+machine);
        }
        logger.info("Categories");
        for(int i=0; i<categoryCodes.length; ++i) {
            logger.info(String.format("  %s %s", categoryCodes[i], categoryNames[i]));
        }
        logger.info("Date");
        logger.info(String.format("  %s - %s", start, end));
    }

    private void _printJobReport() {
        try {
            if(reports.size()>0) {
                File file = new File(root, jobId + ".report");
                PrintWriter pw = new PrintWriter(file);

                AtomicLong total = new AtomicLong();
                Collections.sort(reports);

                reports.forEach(report->{
                    total.addAndGet(report.files);
                    pw.printf("%s\t", report.hash());
                    StringBuilder sb = new StringBuilder(String.format("\t%d files\t%s ~ ", report.files, report.start));
                    if(report.end!=null) {
                        sb.append(report.end).append(" ");
                        sb.append(report.workingMillis).append(" ms ");
                        if(report.retry>1) {
                            sb.append("retry:").append(report.retry-1);
                        }
                    }
                    sb.append(" ").append(report.error);
                    pw.println(sb.toString());
                });

                pw.printf("\nTotal %d threads worked\n", reports.size());
                pw.printf("Total %d files searched\n", total.get());
                pw.flush();
                pw.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected class JobContext implements Callable<List<RSSFtpSearchResponse>> {

        static final int RETRY = 10;
        static final String FAILED_TO_GET_FILE_LIST = "500";

        String fab;
        String machine;
        String categoryName;
        String categoryCode;
        String start;
        String end;
        String keyword;
        String dir;
        String prefix;

        Status status;
        List<RSSFtpSearchResponse> files;
        int depth;

        Report report;

        protected JobContext(String fab, String machine, String categoryName, String categoryCode,
                             String start, String end, String keyword, String dir, int depth) {

            this.fab = fab;
            this.machine = machine;
            this.categoryName = categoryName;
            this.categoryCode = categoryCode;
            this.start = start;
            this.end = end;
            this.keyword = keyword;
            this.dir = dir;
            this.files = new ArrayList<>();
            this.status = Status.ready;
            this.prefix = String.format("(%s|%s|%s) ", machine, categoryName, dir);
            this.depth = depth;
        }

        void getFileList(String fab, String machine, String categoryName, String categoryCode,
                         String start, String end, String keyword, String dir) throws FileSearchException {

            report = new Report(this);
            reports.add(report);

            logger.info(prefix+"...request");

            int retry = 0;
            long startMillis = System.currentTimeMillis();
            LogFileList logFiles;

            while(true) {
                ++retry;
                report.retry = retry;

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    logger.info(prefix + "cancel searching before connection");
                    this.files.clear();
                    return;
                }

                logFiles = connector.getFtpFileList(machine, categoryCode, start, end, keyword, dir,
                        depthLimit!=0?true:false);

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    logger.info(prefix + "cancel searching after connection");
                    this.files.clear();
                    return;
                }

                if (logFiles.getErrorMessage() != null) {
                    logger.error(String.format("%s retry=%d code=%s message=%s", prefix, retry,
                            logFiles.getErrorCode()!=null?logFiles.getErrorMessage():"null",
                            logFiles.getErrorMessage()!=null?logFiles.getErrorMessage():"null"));

                    this.files.clear();
                    status = Status.error;
                    if(retry>RETRY) {
                        logger.error(prefix+"failed to get file list");
                        report.error = FAILED_TO_GET_FILE_LIST;
                        return;
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        logger.info(prefix + "cancel searching before connection");
                        this.files.clear();
                        return;
                    }
                    continue;
                }
                break;
            }

            if (logFiles.getList().length > 0) {
                for (FileInfo file : logFiles.getList()) {

                    if (isDirectory(file) && depth < depthLimit) {
                        if (file.getFilename().endsWith("/.") || file.getFilename().endsWith("/..")) {
                            continue;
                        }

                        try {
                            Date current = dateFormat.parse(file.getTimestamp());
                            if (current.before(startDate) || current.after(endDate)) {
                                continue;
                            }
                        } catch (ParseException e) {
                            logger.error(prefix+"date parsing failed");
                            throw new FileSearchException(FileSearchException.Error.searchFail);
                        }

                        JobContext job = new JobContext(fab, machine, categoryName, categoryCode, start, end, keyword, file.getFilename(), depth + 1);
                        pushJob(job);
                        futures.add(executor.submit(job));
                    } else {
                        if (file.getFilename().endsWith(".") || file.getFilename().endsWith("..")
                                || (!isDirectory(file) && file.getSize() == 0)) {
                            continue;
                        }

                        RSSFtpSearchResponse info = new RSSFtpSearchResponse();
                        info.setFileName(file.getFilename());
                        String[] paths = file.getFilename().split("/");
                        if (paths.length > 1) {
                            int lastIndex = file.getFilename().lastIndexOf("/");
                            info.setFilePath(file.getFilename().substring(0, lastIndex));
                        } else {
                            info.setFilePath(".");
                        }
                        info.setFileSize(file.getSize());
                        info.setFileDate(file.getTimestamp());
                        info.setFileType(file.getType());
                        info.setFabName(fab);
                        info.setMachineName(machine);
                        info.setCategoryName(categoryName);
                        info.setCategoryCode(categoryCode);
                        this.files.add(info);
                    }
                }
            }

            logger.info(String.format("%s...done. %d files/%d ms", prefix, this.files.size(),
                    (System.currentTimeMillis()-startMillis)));
        }

        @Override
        public List<RSSFtpSearchResponse> call() throws Exception {
            try {
                getFileList(fab, machine, categoryName, categoryCode, start, end, keyword, dir);
                searchedCount.addAndGet(files.size());
            } catch (FileSearchException e) {
                logger.error(prefix+"search error "+e.getMessage());
                files.clear();
            }
            finishes.incrementAndGet();
            report.end(files.size());
            return files;
        }
    }

    protected class Report implements Comparable<Report> {
        static final String dateString = "yyyy-MM-dd HH:mm:ss.SSS";
        String machine;
        String category;
        String directory;
        String start;
        String end;
        int depth;
        long startMillis;
        long workingMillis;
        long files;
        long retry = 1;
        String error = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateString);

        Report(JobContext context) {
            this.machine = context.machine;
            this.category = context.categoryCode+"_"+context.categoryName;
            this.directory = context.dir==null?"":context.dir;
            this.depth = context.depth;
            this._start();
        }

        void _start() {
            Calendar current = Calendar.getInstance();
            start = dateFormat.format(current.getTime());
            startMillis = current.getTimeInMillis();
        }

        void end(long files) {
            Calendar current = Calendar.getInstance();
            end = dateFormat.format(current.getTime());
            workingMillis = current.getTimeInMillis()-startMillis;
            this.files = files;
        }

        String hash() {
            return String.format("%s\t%s\t%s", machine, category, directory);
        }

        @Override
        public int compareTo(Report that) {
            int first = machine.compareTo(that.machine);
            if(first==0) {
                int second = category.compareTo(that.category);
                if(second==0) {
                    return directory.compareTo(that.directory);
                }
                return second;
            }
            return first;
        }
    }
}
