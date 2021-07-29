package jp.co.canon.cks.eec.fs.rssportal.background.autocollect;

import jp.co.canon.cks.eec.fs.rssportal.vo.CollectPlanVo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollectExceptionTest {

    @Test
    void test() {
        CollectException ex = new CollectException(new CollectPlanVo());
        assertNotNull(ex.getMessage());
        new CollectException(null, "");
    }
}