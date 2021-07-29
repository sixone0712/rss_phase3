package jp.co.canon.cks.eec.fs.rssportal.background;

import jp.co.canon.ckbs.eec.fs.collect.FileServiceCollectConnectorFactory;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnector;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.ConfigurationService;
import jp.co.canon.cks.eec.fs.rssportal.background.fileserviceproc.*;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import jp.co.canon.cks.eec.fs.rssportal.common.Tool;
import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FileDownloadExecutor {

    private final EspLog log;

    private enum Status {
        idle, init, download, compress, complete, stop, error
    }

    private static int mUniqueKey = 1;
    @Getter
    private final String jobType;
    @Getter
    private final String ftpType;
    private final String desc;
    private final String downloadId;
    private Status status;
    private List<DownloadRequestForm> downloadForms;
    private List<FileDownloadContext> downloadContexts;
    private List<FileDownloadContext> timeoutContexts;
    private final String baseDir;

    private final FileDownloader downloader;
    private final FileServiceManageConnector fileServiceManageConnector;
    private final FileServiceCollectConnectorFactory fileServiceCollectConnectorFactory;
    private final ConfigurationService configurationService;

    private int totalFiles = -1;
    private String mPath = null;
    private long lastUpdate;
    private long errorCount;

    private boolean attrStrictError;
    private boolean attrCompression;
    private boolean attrEmptyAllPathBeforeDownload;
    private boolean attrReplaceFileForSameFileName;
    private boolean attrDownloadFilesViaMultiSessions;

    private Thread thread;

    public FileDownloadExecutor(
            @NonNull final String jobType,
            @NonNull final String ftpType,
            @Nullable final String desc,
            @NonNull final FileDownloader downloader,
            @NonNull final List<DownloadRequestForm> request,
            boolean compress) {

        if(desc==null) {
            log = new EspLog(getClass());
        } else {
            String fmt = "%s:%s";
            log = new EspLog(String.format(fmt, getClass().toString(), desc));
        }

        this.jobType = jobType;
        this.ftpType = ftpType;
        this.downloader = downloader;
        this.fileServiceManageConnector = downloader.getFileServiceManageConnector();
        this.fileServiceCollectConnectorFactory = downloader.getFileServiceCollectConnectorFactory();
        this.configurationService = downloader.getConfigurationService();

        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        downloadId = "DL"+(mUniqueKey++)+stamp.getTime();
        downloadForms = request;
        downloadContexts = new ArrayList<>();
        timeoutContexts = new ArrayList<>();
        baseDir = Paths.get(downloader.getDownloadCacheDir(), downloadId).toString();

        this.desc = desc==null?"noname":desc;

        this.errorCount = 0;
        if(this.ftpType.equals("vftp_compat")) {
            this.attrStrictError = false;
        } else {
            this.attrStrictError = true;
        }
        this.attrCompression = compress;
        this.attrEmptyAllPathBeforeDownload = true;
        this.attrReplaceFileForSameFileName = false;
        this.attrDownloadFilesViaMultiSessions = false;
        setStatus(Status.idle);
    }

    private void initialize() {
        setStatus(Status.init);
        log.info(downloadId+": initialize()");
        for (DownloadRequestForm f : downloadForms) {
            if(f instanceof FtpDownloadRequestForm) {
                FtpDownloadRequestForm form = (FtpDownloadRequestForm)f;
                if (form.getFtpType().equals("ftp") && form.getFiles().size() == 0)
                    continue;
            }

            FileDownloadContext context = new FileDownloadContext(ftpType, downloadId, f, baseDir);
            context.setFileServiceManageConnector(fileServiceManageConnector);
            switch (ftpType) {
                default :
                    log.error("undefined ftp-type  "+ftpType);
                    setStatus(Status.error);
                    return;
                case "ftp":
                case "vftp_sss":
                    context.setAchieve(true);
                    /*if(jobType.equals(JobType.manual.name())) {
                        context.setAchieveDecompress(false);
                    } else {
                        context.setAchieveDecompress(true);
                    }*/
                    context.setAchieveDecompress(false);
                    break;
                case "vftp_compat":
                    context.setAchieve(false);
                    /*if(jobType.equals(JobType.plan.name())) {
                        context.setAchieveDecompress(false);
                    }*/
                    break;
            }
            downloadContexts.add(context);
        }
        totalFiles = 0;
        downloadContexts.forEach(context -> totalFiles += context.getFileCount());
    }

    private void compress() {
        if(status==Status.error || status==Status.stop) {
            log.info(downloadId+": compress() skip");
            return;
        }
        log.info(downloadId+": compress()");
        setStatus(Status.compress);

        Compressor comp = new Compressor();
        String fileName = String.format("%d.zip", System.currentTimeMillis());
        String zipDir = Paths.get(downloader.getDownloadResultDir(), downloadId, fileName).toString();
        if(comp.compress(baseDir, zipDir)) {
            mPath = zipDir;
        }
    }

    private void wrapup() {
        if(status==Status.error || status==Status.stop) {
            log.info(downloadId+": wrapup() skip");
            return;
        }
        log.info(downloadId+": wrapup()");

        // When the executor didn't compress the downloads then the result path is null.
        // Because wrapup() means this download has finished successfully.
        // Therefore we have to fill the download path information below.
        if(mPath==null) {
            if(downloadContexts.size()==1) {
                FileDownloadContext context = downloadContexts.get(0);
                File destination = new File(context.getLocalFilePath());
                if(!destination.exists()) {
                    log.error("destination doesn't exist  "+destination.toString());
                    setStatus(Status.error);
                    return;
                }
                if(destination.isDirectory()) {
                    File[] files = destination.listFiles();
                    if(files.length!=1 || (files.length>0 && files[0].isDirectory())) {
                        log.error("cannot specify destination");
                        setStatus(Status.error);
                        return;
                    } else {
                        mPath = files[0].toString();
                    }
                } else {
                    mPath = destination.toString();
                }
            } else {
                log.error("download config error");
                setStatus(Status.error);
                return;
            }
        }
        log.info("output destination="+mPath);
        setStatus(Status.complete);
    }

    private class DownloadRunner implements Runnable {

        private EspLog log;
        private final AtomicInteger runnings;
        private int maxThreads;

        public DownloadRunner(EspLog log) {
            this.log = log;
            runnings = new AtomicInteger(0);
            maxThreads = downloader.getMaxThreads();
            if(maxThreads>64) {
                maxThreads = 64;
            } else if(maxThreads<1) {
                maxThreads = 1;
            }
        }

        @Override
        public void run() {
            try {
                initialize();

                if(status==Status.stop) {
                    downloader.finishRequest(downloadId);
                    return;
                }

                log.info("download start");
                setStatus(Status.download);
                requestContextDownload(downloadContexts, process->{
                    if(process.getStatus()==FileDownloadServiceProc.Status.Error) {
                        ++errorCount;
                        if(attrStrictError) {
                            status = Status.error;
                        } else if(errorCount>=downloadContexts.size()) {
                            status = Status.error;
                        }
                        if(status==Status.error) {
                            log.error("download error occurs");
                        }
                        int currentThreads = runnings.decrementAndGet();
                        log.error("download done with error. "+currentThreads+" threads is running");
                    } else if(process.getStatus()==FileDownloadServiceProc.Status.Timeout) {
                        timeoutContexts.add(process.getContext());
                        int currentThreads = runnings.decrementAndGet();
                        log.warn("download timeout occurs. "+currentThreads+" threads is running");
                    } else if(process.getStatus()==FileDownloadServiceProc.Status.Finished) {
                        int currentThreads = runnings.decrementAndGet();
                        log.info("download done. "+currentThreads+" threads is running");
                    }
                });

                log.info("download timeout contexts timeouts="+timeoutContexts.size());
                requestContextDownload(timeoutContexts, process->{
                    int currentThreads;
                    FileDownloadServiceProc.Status processStatus = process.getStatus();
                    if(processStatus==FileDownloadServiceProc.Status.Error || processStatus==FileDownloadServiceProc.Status.Timeout) {
                        ++errorCount;
                        if(attrStrictError) {
                            status = Status.error;
                        } else if(errorCount>=downloadContexts.size()) {
                            status = Status.error;
                        }
                        if(status==Status.error) {
                            log.error("download error occurs");
                        }
                        currentThreads = runnings.decrementAndGet();
                        log.error("download done with error. "+currentThreads+" threads is running");
                    } else if(processStatus==FileDownloadServiceProc.Status.Finished) {
                        currentThreads = runnings.decrementAndGet();
                        log.info("download done. "+currentThreads+" threads is running");
                    }
                });

                if(attrCompression) {
                    compress();
                }
                wrapup();
            } catch (InterruptedException e) {
                log.error("unexpected interrupt occur");
            }
            downloader.finishRequest(downloadId);
        }

        private void requestContextDownload(List<FileDownloadContext> contexts, FileDownloadServiceCallback callback) throws InterruptedException {
            List<FileDownloadServiceProc> procs = new ArrayList<>();

            context_loop:
            for(FileDownloadContext context: contexts) {
                if (status == Status.stop || status == Status.error) {
                    break;
                }

                while (runnings.get() >= maxThreads) {
                    log.info("waiting thread free");
                    Thread.sleep(1000);
                    if (status == Status.stop || status == Status.error) {
                        break context_loop;
                    }
                }

                FileDownloadHandler handler;
                switch(ftpType) {
                    default:
                    case "ftp":
                        handler = new FtpFileDownloadHandler(fileServiceManageConnector,
                                fileServiceCollectConnectorFactory, configurationService,
                                context.getTool(), context.getLogType(), context.getFileNames());
                        break;
                    case "vftp_compat":
                        handler = new VFtpCompatFileDownloadHandler(fileServiceManageConnector,
                                fileServiceCollectConnectorFactory, configurationService,
                                context.getTool(), context.getCommand());
                        break;
                    case "vftp_sss":
                        handler = new VFtpSssFileDownloadHandler(fileServiceManageConnector,
                                fileServiceCollectConnectorFactory, configurationService,
                                context.getTool(), context.getDirectory(), context.getFileNames());
                        break;
                }
                procs.add(new FileDownloadServiceProc(handler, context, callback));
                runnings.getAndIncrement();
            }

            thread_wait:
            for (FileDownloadServiceProc proc : procs) {
                while(true) {
                    if (status == Status.stop || status == Status.error) {
                        log.info("stop waiting threads");
                        break thread_wait;
                    }
                    if (proc.getStatus() == FileDownloadServiceProc.Status.InProgress) {
                        Thread.sleep(100);
                        continue;
                    } else if(proc.getStatus()==FileDownloadServiceProc.Status.Finished) {
                        continue thread_wait;
                    } else if(proc.getStatus()==FileDownloadServiceProc.Status.Error) {
                        log.warn(proc.getName()+" error occurred");
                        continue thread_wait;
                    } else if(proc.getStatus()== FileDownloadServiceProc.Status.Timeout) {
                        log.warn(proc.getName()+" timeout occurred");
                        continue thread_wait;
                    }
                }
            }

            log.info("threads terminate");
            for (FileDownloadServiceProc proc : procs) {
                proc.interrupt();
                proc.join();
            }
        }
    }

    private void setStatus(Status status) {
        this.status = status;
        lastUpdate = System.currentTimeMillis();
    }

    private void printExecutorInfo() {
        log.info("FileDownloadExecutor(desc="+desc+", id="+downloadId+")");
        log.info("attr."+attrCompression);
        log.info("    .DownloadFilesViaMultiSessions="+attrDownloadFilesViaMultiSessions);
        log.info("    .EmptyAllPathBeforeDownload"+attrEmptyAllPathBeforeDownload);
        log.info("    .ReplaceFileForSameFileName"+attrReplaceFileForSameFileName);
        log.info("path.base="+baseDir);
        log.info("download");
        for(DownloadRequestForm form: downloadForms) {
            if(form instanceof VFtpCompatDownloadRequestForm) {
                log.info("    " + form.getMachine() + " / " + ((VFtpCompatDownloadRequestForm) form).getCommand());
            } else if(form instanceof VFtpSssDownloadRequestForm) {
                log.info("    " + form.getMachine() + " / " + ((VFtpSssDownloadRequestForm) form).getDirectory() +
                        " ("+((VFtpSssDownloadRequestForm)form).getFiles().size()+" files)");
            } else {
                log.info("    " + form.getMachine() + " / " + ((FtpDownloadRequestForm)form).getCategoryType() +
                        " (" + ((FtpDownloadRequestForm)form).getFiles().size() + " files)");

                /*FtpDownloadRequestForm ftpForm = (FtpDownloadRequestForm)form;
                long last = ftpForm.getLastTimestamp();
                for(FileInfo f:ftpForm.getFiles()) {
                    if(f.getMilliTime()==last) {
                        log.info("      - " + f.getName() + " " + f.getSize() + " " + f.getDate() + " <-- lastTimestamp");
                    } else {
                        log.info("      - " + f.getName() + " " + f.getSize() + " " + f.getDate());
                    }
                }*/
            }
        }
    }

    public String getId() {
        return downloadId;
    }

    public void start() {
        log.info("file download start ("+ downloadForms.size()+")");
        printExecutorInfo();

        thread = new Thread(new DownloadRunner(log));
        thread.setName("FileDownloadExecutor-"+downloadId);
        thread.start();
    }

    public void finish() {
        log.info("finish "+downloadId);
        if(thread!=null) {
            try {
                thread.join(30000);
                log.info("thread "+thread.getName()+" joined");
            } catch (InterruptedException e) {
                log.warn("thread "+thread.getName()+" interrupt occured");
            }
        }
        downloadForms = null;
        downloadContexts = null;
        timeoutContexts = null;
        Tool.deleteDir(new File(baseDir));
    }

    public void stop() {
        log.info("stop downloading");
        setStatus(Status.stop);
    }

    public boolean isRunning() {
        return (status==Status.complete || status==Status.error || status==Status.stop)?false:true;
    }

    public String getStatus() {
        return status.toString();
    }


    public String getBaseDir() {
        return baseDir;
    }

    public String getDownloadPath() {
        return mPath;
    }

    public long getDownloadFiles() {
        AtomicLong files = new AtomicLong();
        downloadContexts.forEach(context->{
            FileDownloadInfo info = context.getDownloadInfo();
            if(info!=null) {
                files.addAndGet(info.getDownloadFiles());
            }
        });
        return files.get();
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public long getDownloadSize() {
        AtomicLong size = new AtomicLong();
        downloadContexts.forEach(context->{
            FileDownloadInfo info = context.getDownloadInfo();
            if(info!=null) {
                size.addAndGet(info.getDownloadBytes());
            }
        });
        return size.get();
    }

    public long getTotalSize() {
        AtomicLong size = new AtomicLong();
        downloadContexts.forEach(context->{
            size.addAndGet(context.getTotalFileSize());
        });
        return size.get();
    }

    public List<String> getFabs() {
        List<String> fabs = new ArrayList<>();
        for(DownloadRequestForm form: downloadForms) {
            String fab = form.getFab();
            if(!fabs.contains(fab))
                fabs.add(fab);
        }
        return fabs;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    private boolean isAchieveJobType() {
        if(ftpType.equals("vftp_sss"))
            return false;
        return true;
    }

    /* Attributes */
    public boolean isAttrCompression() {
        return attrCompression;
    }

    public void setAttrCompression(boolean attrCompression) {
        this.attrCompression = attrCompression;
    }

    public boolean isAttrEmptyAllPathBeforeDownload() {
        return attrEmptyAllPathBeforeDownload;
    }

    public void setAttrEmptyAllPathBeforeDownload(boolean attrEmptyAllPathBeforeDownload) {
        this.attrEmptyAllPathBeforeDownload = attrEmptyAllPathBeforeDownload;
    }

    public boolean isAttrReplaceFileForSameFileName() {
        return attrReplaceFileForSameFileName;
    }

    public void setAttrReplaceFileForSameFileName(boolean attrReplaceFileForSameFileName) {
        this.attrReplaceFileForSameFileName = attrReplaceFileForSameFileName;
    }

    public boolean isAttrDownloadFilesViaMultiSessions() {
        return attrDownloadFilesViaMultiSessions;
    }

    public void setAttrDownloadFilesViaMultiSessions(boolean attrDownloadFilesViaMultiSessions) {
        this.attrDownloadFilesViaMultiSessions = attrDownloadFilesViaMultiSessions;
    }
}
