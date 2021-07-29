package jp.co.canon.cks.eec.fs.rssportal.model.ftp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RSSFtpSearchResponseTest {

    @Test
    void test() {
        RSSFtpSearchResponse response = new RSSFtpSearchResponse();
        String smile = ":)";
        response.setFabName(smile);
        assertEquals(smile, response.getFabName());
        response.setMachineName(smile);
        assertEquals(smile, response.getMachineName());
        response.setCategoryName(smile);
        assertEquals(smile, response.getCategoryName());
        response.setCategoryCode(smile);
        assertEquals(smile, response.getCategoryCode());
        response.setFileName(smile);
        assertEquals(smile, response.getFileName());
        response.setFileSize(10);
        assertEquals(10, response.getFileSize());
        response.setFileDate(smile);
        assertEquals(smile, response.getFileDate());
        response.setFilePath(smile);
        assertEquals(smile, response.getFilePath());
        response.setFileType(smile);
        assertEquals(smile, response.getFileType());

    }

}