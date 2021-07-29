package jp.co.canon.cks.eec.fs.rssportal.background.fileserviceproc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileDownloadHandlerTest {

    @Test
    void test() {
        {
            FtpFileDownloadHandler h = new FtpFileDownloadHandler(null, null, null, null, null, null);
            assertNotNull(h);
            h.cancelDownloadRequest();
            assertNull(h.getDownloadedFiles());
        }
        {
            VFtpCompatFileDownloadHandler h = new VFtpCompatFileDownloadHandler(null, null, null, null, null);
            assertNotNull(h);
            h.cancelDownloadRequest();
            assertNull(h.getDownloadedFiles());
        }
        {
            VFtpSssFileDownloadHandler h = new VFtpSssFileDownloadHandler(null, null, null, null, null, null);
            assertNotNull(h);
            h.cancelDownloadRequest();
            assertNull(h.getDownloadedFiles());
        }
    }
}