package jp.co.canon.cks.eec.fs.rssportal.background;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FtpWorkerTest {

    @Test
    void test() {
        FtpWorker ftp = new FtpWorker("host", 12345, "mode");
        assertNotNull(ftp);
        assertNotNull(new FtpWorker("host", 12345));
        assertFalse(ftp.transfer(":)", "=)"));
    }

}