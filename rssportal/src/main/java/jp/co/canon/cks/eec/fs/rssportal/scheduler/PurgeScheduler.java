package jp.co.canon.cks.eec.fs.rssportal.scheduler;

import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import jp.co.canon.cks.eec.fs.rssportal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class PurgeScheduler {
    private static UserService userService = null;
    private final EspLog log = new EspLog(getClass());

    @Autowired
    public PurgeScheduler(UserService userService) {
        PurgeScheduler.userService = userService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteExpiredToken() {
        log.info("[Scheduler] execute the process that delete expired token in the blacklist");
        userService.cleanBlacklist(new Date());
    }
}
