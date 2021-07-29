package jp.co.canon.cks.eec.fs.rssportal.background.localfs;

import jp.co.canon.cks.eec.fs.rssportal.background.FileDownloader;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class DownloadFileSystemMonitor extends FileSystemMonitor {

    private final EspLog log = new EspLog(getClass());
    private static final String name = "download-fs-monitor";
    @Value("${rssportal.collect.resultBase}")
    private String path;
    @Value("${rssportal.purger.file-downloader.min-size}")
    private int _minFreeSpace;    // gigabytes
    @Value("${rssportal.purger.file-downloader.min-percent}")
    private int _minFreeSpacePercent;
    @Value("${rssportal.purger.file-downloader.interval}")
    private long _interval;
    @Value("${rssportal.purger.file-downloader.keeping-period}")
    private long keepingPeriod;
    private long keepingPeriodMillis;


    private final FileDownloader fileDownloader;
    
    private List<File> invalidFileList;

    @Autowired
    public DownloadFileSystemMonitor(FileDownloader fileDownloader) {
        super(name);
        this.fileDownloader = fileDownloader;
        invalidFileList = new ArrayList<>();
    }

    @PostConstruct
    public void postConstruct() {
        configure(path, _minFreeSpace, _minFreeSpacePercent, _interval*1000);
        keepingPeriodMillis = keepingPeriod*3600000;
        log.info(name+" thread starts");
        log.info(String.format("  interval=%d s keep=%d s", _interval, keepingPeriod));
    }

    @Override
    protected boolean checkSpecial() {
        File target = new File(path);
        invalidFileList.clear();
        for(File file: target.listFiles()) {
            log.info(monitorName+" : "+file.getName());
            String downloadId = file.getName();
            long cur = System.currentTimeMillis();
            long update = fileDownloader.getLastUpdateTime(downloadId);
            if(fileDownloader.getStatus(downloadId).equalsIgnoreCase("invalid-id")) {
                invalidFileList.add(file);
            } else if((cur-update)>keepingPeriodMillis) {
                invalidFileList.add(file);
            }
        }
        return invalidFileList.size()==0?false:true;
    }

    @Override
    protected void cleanup() {
        if(invalidFileList.size()==0) {
            log.warn("no file to cleanup");
            return;
        }
        for(File file: invalidFileList) {
            deleteDir(file);
            log.info("downloaded file "+file.getName()+" deleted");
        }
    }

    @Override
    protected void restart() {
    }

    @Override
    protected void halt() {
    }

    @Override
    protected boolean errorHandler(String error) {
        return false;
    }

    @Override
    protected void report(long total, long usable) {
        log.info("filesystem report for "+name);
        log.info("+ total : "+gigabytes(total)+" GB");
        log.info("+ usable : "+gigabytes(usable)+" GB");
        log.info("+ "+percent(total, usable)+" % free");
    }
}
