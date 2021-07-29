package jp.co.canon.cks.eec.fs.rssportal.background.autocollect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollectMpaExceptionTest {

    @Test
    void test() {
        new CollectMpaException(":)", "=)");
        CollectMpaException ex = new CollectMpaException(":)");
        assertNotNull(ex);
        assertNotNull(ex.getMachine());
    }
}