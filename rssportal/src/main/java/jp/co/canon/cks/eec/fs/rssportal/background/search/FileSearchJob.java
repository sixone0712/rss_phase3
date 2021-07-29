package jp.co.canon.cks.eec.fs.rssportal.background.search;

import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnector;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import jp.co.canon.cks.eec.fs.rssportal.common.FileLog;
import jp.co.canon.cks.eec.fs.rssportal.model.ftp.RSSFtpSearchResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.File;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Getter
abstract public class FileSearchJob extends Thread {

    public enum Status {
        ready,
        inProgress,
        done,
        error
    }

    protected final FileServiceManageConnector connector;
    protected final String  jobId;
    protected String        jobType;
    protected Calendar      requestTime;
    protected long          requestMillis;
    protected Calendar      finishTime;
    protected long          operatingMillis;
    protected Calendar      lastAccess;
    protected String        status;
    protected int           maxThreads;
    protected String        root;
    protected boolean       canceling;
    protected boolean       dead;

    protected String[]      fabNames;
    protected String[]      machineNames;

    protected FileLog logger;
    protected List<Future<List<RSSFtpSearchResponse>>> futures;
    protected ExecutorService executor;

    protected AtomicLong searchedCount;
    protected AtomicLong works;
    protected AtomicLong finishes;

    abstract protected void distribute();
    abstract protected void harvest();
    abstract protected void printout();
    abstract protected List getSearchFiles();

    public FileSearchJob(
            FileServiceManageConnector connector,
            String jobId,
            String[] fabNames,
            String[] machineNames,
            String root,
            int maxThreads
    ) throws ParseException {

        this.connector = connector;
        this.jobId = jobId;
        this.maxThreads = maxThreads;
        this.root = root;
        this.canceling = false;
        this.dead = false;

        this.fabNames = fabNames;
        this.machineNames = machineNames;

        this.requestTime = Calendar.getInstance();
        this.lastAccess = this.requestTime;
        requestMillis = requestTime.getTimeInMillis();

        File rootFile = Paths.get(root).toFile();
        if(rootFile.exists() && rootFile.isFile()) {
            rootFile.delete();
        }
        if(!rootFile.exists()) {
            rootFile.mkdirs();
        }

        logger = new FileLog.Builder()
                .file(Paths.get(this.root, this.jobId+".log").toFile())
                .name("search-"+this.jobId)
                .build();

        executor = Executors.newFixedThreadPool(this.maxThreads);
        futures = new ArrayList<>();
        searchedCount = new AtomicLong(0);
        works = new AtomicLong(0);
        finishes = new AtomicLong(0);

        logger.info("search job"+this.jobId+" starts");
    }

    public FileSearchInfo getInfo() {
        FileSearchInfo info = new FileSearchInfo();
        info.setSearchId(jobId);
        info.setSearchedCnt(searchedCount.get());
        String infoStatus = FileSearchManager.IN_PROGRESS;
        if(status.equals(FileSearchManager.ERROR)) {
            infoStatus = FileSearchManager.ERROR;
        } else if(status.equals(FileSearchManager.DONE)) {
            // The thread may be composing the report right after status changes to `done`.
            if(dead) {
                infoStatus = FileSearchManager.DONE;
                info.setResultUrl(createResultUrl(jobId, jobType));
            }
        }
        info.setStatus(infoStatus);
        updateLastAccess();
        return info;
    }

    public void cancel() {
        logger.info("request to cancel searching");
        canceling = true;
        try {
            if(!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                logger.warn("shutdownNow!");
                List<Runnable> awaits = executor.shutdownNow();
                logger.warn(awaits.size()+" threads are awaiting executions");
            }
        } catch (InterruptedException e) {
            logger.error("interrupt occurs when cancel");
        }
        updateLastAccess();
    }

    public static String createResultUrl(String jobId, String jobType) {
        if(jobType.equals(FileSearchManager.FTP_TYPE)) {
            return "/rss/api/ftp/search/result/"+jobId;
        } else if(jobType.equals(FileSearchManager.VFTP_SSS_TYPE)){
            return "/rss/api/vftp/sss/search/result/"+jobId;
        }
        return null;
    }

    protected void submitProcess(Callable callable) {
        works.incrementAndGet();
        futures.add(executor.submit(callable));
    }

    @Override
    public void run() {

        preprocess();

        distribute();

        watch();

        harvest();

        postprocess();

        printout();

        shutdown();
    }

    private void preprocess() {
        status = FileSearchManager.IN_PROGRESS;
    }

    private void postprocess() {
        finishTime = Calendar.getInstance();
        operatingMillis = finishTime.getTimeInMillis()-requestMillis;
        logger.info("finish the job files="+getSearchFiles().size()
                +"/"+operatingMillis+"ms");
        status = FileSearchManager.DONE;
    }

    private void watch() {
        int c = 0;
        while(works.get()!=finishes.get()) {
            ++c;
            try {
                sleep(50);
            } catch (InterruptedException e) {
                logger.info("canceled searching");
                break;
            }
            if(c>60) {
                c = 0;
                logger.info("job status "+finishes.get()+"/"+works.get());
            }
        }
    }

    private void shutdown() {
        logger.info("shutdown");
        executor.shutdown();
        try {
            if(!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.warn("shutdownNow!");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warn("shutdownNow!!");
            executor.shutdownNow();
        }
        dead = true;
        shutdownAction();
        logger.info("shutdown done");
    }

    private void updateLastAccess() {
        lastAccess = Calendar.getInstance();
    }

    protected void shutdownAction() {
        // Override this
    }


    @Setter @Accessors(chain = true, fluent = true)
    public static class Builder {

        private FileServiceManageConnector connector;
        private String      root;
        private String      jobType;
        private String[]    fabNames;
        private String[]    machineNames;
        private int         threadNumbers = 4;
        private int         depthLimit = 999;

        private String[]    categoryNames;
        private String[]    categoryCodes;
        private String      start;
        private String      end;

        private String      command;

        public FileSearchJob build(long key) throws FileSearchException {

            if(jobType.equals(FileSearchManager.FTP_TYPE)) {
                if (fabNames == null || machineNames == null || categoryCodes == null || categoryNames == null
                        || start == null || end == null || fabNames.length == 0 || machineNames.length == 0
                        || categoryNames.length == 0 || categoryCodes.length == 0
                        || connector == null) {

                    throw new FileSearchException(FileSearchException.Error.invalidParam);
                }
                String id = createJobId(key);
                try {
                    return new FtpFileSearchJob(connector, id, fabNames, machineNames, categoryNames, categoryCodes,
                            start, end, root, threadNumbers, depthLimit);
                } catch (ParseException e) {
                }
            } else if(jobType.equals(FileSearchManager.VFTP_SSS_TYPE)) {
                if(fabNames==null||machineNames==null||command==null) {
                    throw new FileSearchException(FileSearchException.Error.invalidParam);
                }
                String id = createJobId(key);
                try {
                    return new VFtpFileSearchJob(connector, id, fabNames, machineNames, command, root, threadNumbers);
                } catch (ParseException e) {
                }
            }
            return null;
        }

        public FileSearchJob build() throws FileSearchException {
            return build(1);
        }

        private String createJobId(long key) {
            final String format = "%s_%05d";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String currentTime = dateFormat.format(new Date(System.currentTimeMillis()));
            return String.format(format, currentTime, key);
        }
    }
}
