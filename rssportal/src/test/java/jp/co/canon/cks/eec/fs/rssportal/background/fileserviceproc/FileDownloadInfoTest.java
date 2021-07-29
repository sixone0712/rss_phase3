package jp.co.canon.cks.eec.fs.rssportal.background.fileserviceproc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileDownloadInfoTest {

    @Test
    void test() {
        FileDownloadInfo f = new FileDownloadInfo();
        f.setDownloadBytes(1);
        assertEquals(1, f.getDownloadBytes());
        f.setDownloadFiles(1);
        assertEquals(1, f.getDownloadFiles());
        f.setError(false);
        assertEquals(false, f.isError());
        f.setFinish(false);
        assertEquals(false, f.isFinish());
        f.setRequestBytes(1);
        assertEquals(1, f.getRequestBytes());
        f.setRequestFiles(1);
        assertEquals(1, f.getRequestFiles());
    }

}