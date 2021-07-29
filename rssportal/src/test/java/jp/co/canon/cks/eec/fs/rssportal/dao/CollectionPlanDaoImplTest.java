package jp.co.canon.cks.eec.fs.rssportal.dao;

import jp.co.canon.cks.eec.fs.rssportal.vo.CollectPlanVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CollectionPlanDaoImplTest {

    @Autowired
    private CollectionPlanDao dao;

    @Test
    void test() {
        assertNotNull(dao);
        assertNotNull(dao.findAll(true, 0));
        assertNotNull(dao.findAll(false, 1));

        assertTrue(dao.updatePlan(new CollectPlanVo()));
    }

}