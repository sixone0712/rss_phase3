package jp.co.canon.cks.eec.fs.rssportal.background;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FileDownloadContextTest {

    @Test
    void test() {
        FileDownloadContext c = new FileDownloadContext("ftpType", "id",
                new DownloadRequestForm("ftpType", "fab", "machine"), "base");
        assertNotNull(c);
        assertFalse(c.isDownloadComplete());
        assertFalse(c.isFtpProcComplete());
        c.setRootDir(new File(":)"));
        assertNotNull(c.getRootDir());
        c.getFileSizes();
        c.getFileDates();
        assertNull(c.getRequestNo());
        c.setDownloadFiles(10);
        assertEquals(10, c.getDownloadFiles());
        c.getConnector();
        c.isAchieve();
    }
}