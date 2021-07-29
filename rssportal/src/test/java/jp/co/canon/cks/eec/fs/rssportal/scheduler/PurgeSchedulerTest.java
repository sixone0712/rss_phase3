package jp.co.canon.cks.eec.fs.rssportal.scheduler;

import jp.co.canon.cks.eec.fs.rssportal.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PurgeSchedulerTest {

    @Autowired
    private UserService service;

    @Test
    void test() {
        assertNotNull(service);
        PurgeScheduler scheduler = new PurgeScheduler(service);
        scheduler.deleteExpiredToken();
    }

}