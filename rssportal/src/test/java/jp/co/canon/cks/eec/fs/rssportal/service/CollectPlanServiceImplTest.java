package jp.co.canon.cks.eec.fs.rssportal.service;

import jp.co.canon.cks.eec.fs.rssportal.vo.CollectPlanVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CollectPlanServiceImplTest {

    @Autowired
    private CollectPlanService service;

    @Test
    void test() {
        assertNotNull(service);
        service.isReady();
        assertFalse(service.deletePlan(new CollectPlanVo()));
        assertNotNull(service.getAllPlans());
        assertNull(service.getAllPlansBySchedulePriority());
        assertNull(service.getNextPlan());
        service.scheduleAllPlans();
        service.updateLastCollect(null);
        service.addNotifier(null);
        service.setLastStatus(0, null);
        assertEquals(-1, service.modifyPlan(-1, "ftp", 0, ":)", new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new Date(), new Date(), new Date(), ":)", 0, "", false));
        assertEquals(-1, service.modifyPlan(-1, "vftp_compat", 0, ":)", new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new Date(), new Date(), new Date(), ":)", 0, "", false));


    }

}