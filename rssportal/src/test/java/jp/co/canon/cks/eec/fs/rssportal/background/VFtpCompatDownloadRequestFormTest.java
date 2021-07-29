package jp.co.canon.cks.eec.fs.rssportal.background;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VFtpCompatDownloadRequestFormTest {

    @Test
    void test() {
        VFtpCompatDownloadRequestForm f = new VFtpCompatDownloadRequestForm("", "", "", false);
        f.setCommand(":)");
        assertEquals(":)", f.getCommand());
        f.setDecompress(true);
        assertTrue(f.isDecompress());
        assertNotNull(f.getMachine());
        assertNotNull(f.getFab());
        assertNotNull(f.getFtpType());
    }

}