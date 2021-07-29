package jp.co.canon.cks.eec.fs.rssportal.background.localfs;

import jp.co.canon.cks.eec.fs.rssportal.background.autocollect.PlanManager;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import jp.co.canon.cks.eec.fs.rssportal.downloadlist.DownloadListService;
import jp.co.canon.cks.eec.fs.rssportal.downloadlist.DownloadListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
public class CollectPlanFileSystemMonitor extends FileSystemMonitor {

    private final EspLog log = new EspLog(getClass());
    private static final String name = "collect-plan-fs-monitor";

    @Value("${rssportal.collect.logBase}")
    private String _path;
    @Value("${rssportal.purger.collect-plan.min-size}")
    private int _minFreeSpace;    // gigabytes
    @Value("${rssportal.purger.collect-plan.min-percent}")
    private int _minFreeSpacePercent;
    @Value("${rssportal.purger.collect-plan.interval}")
    private long _interval;
    @Value("${rssportal.purger.collect-plan.keeping-period}")
    private long _keepPeriod;
    private long keepPeriodMillis;

    private final DownloadListService downloadService;
    private final PlanManager manager;

    private List<DownloadListVo> cleanupList;

    @Autowired
    public CollectPlanFileSystemMonitor(DownloadListService downloadService, PlanManager manager) {
        super(name);
        this.downloadService = downloadService;
        this.manager = manager;
        cleanupList = new ArrayList<>();
    }

    @PostConstruct
    public void postConstruct() {
        configure(_path, _minFreeSpace, _minFreeSpacePercent, _interval*1000);
        keepPeriodMillis = _keepPeriod*3600000;
        log.info(name+" thread starts");
        log.info(String.format("  interval=%d s keep=%d s", _interval, _keepPeriod));
    }

    @Override
    protected boolean isReady() {
        if(!downloadService.isReady()) {
            log.error("download_list table is not ready");
            return false;
        }
        return true;
    }

    @Override
    protected boolean checkSpecial() {
        List<DownloadListVo> list = downloadService.getFinishedList();
        cleanupList.clear();
        for(DownloadListVo item: list) {
            Timestamp keepPoint = new Timestamp(System.currentTimeMillis()-keepPeriodMillis);
            if(item.getCreated().before(keepPoint)) {
                cleanupList.add(item);
            }
        }
        return cleanupList.size()>0?true:false;
    }

    @Override
    protected void cleanup() {
        if(cleanupList.size()==0) {
            log.warn("no item to cleanup");
            return;
        }
        for(DownloadListVo item: cleanupList) {
            File file = new File(item.getPath());
            if(file.exists()) {
                file.delete();
            }
            downloadService.delete(item.getId());
            log.info("downloadlist/"+item.getPlanId()+"/"+item.getTitle()+" deleted");
        }
    }

    @Override
    protected void restart() {
        log.info("restart collecting");
        manager.setHalted(false);
    }

    @Override
    protected void halt() {
        log.info("halt collecting");
        manager.setHalted(true);
    }

    @Override
    protected boolean errorHandler(String error) {
        log.error(error);
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
