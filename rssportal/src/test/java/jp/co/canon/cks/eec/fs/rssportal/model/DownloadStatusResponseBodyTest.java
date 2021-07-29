package jp.co.canon.cks.eec.fs.rssportal.model;

import jp.co.canon.cks.eec.fs.rssportal.background.FileDownloader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DownloadStatusResponseBodyTest {

    @Autowired
    private FileDownloader downloader;

    @Test
    void test() {
        assertNotNull(downloader);

        DownloadStatusResponseBody body = new DownloadStatusResponseBody(downloader, ":)");
        assertNotNull(body);

        body.setStatus(":)");
        assertEquals(":)", body.getStatus());

        body.setDownloadedFiles(11);
        assertEquals(11, body.getDownloadedFiles());

        body.setDownloadId(":)");
        assertEquals(":)", body.getDownloadId());

        body.setTotalFiles(20);
        assertEquals(20, body.getTotalFiles());
    }
}