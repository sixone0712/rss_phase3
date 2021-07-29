package jp.co.canon.cks.eec.fs.rssportal.model.ftp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RSSFtpSearchRequestTest {

    @Test
    void test() {
        RSSFtpSearchRequest request = new RSSFtpSearchRequest();

        String smile = ":)";
        request.setCategoryCode(smile);
        request.setCategoryName(smile);
        request.setDir(smile);
        request.setStartDate(smile);
        request.setEndDate(smile);
        request.setFabName(smile);
        request.setKeyword(smile);
        request.setMachineName(smile);
        assertEquals(smile, request.getCategoryCode());
        assertEquals(smile, request.getCategoryName());
        assertEquals(smile, request.getDir());
        assertEquals(smile, request.getStartDate());
        assertEquals(smile, request.getEndDate());
        assertEquals(smile, request.getFabName());
        assertEquals(smile, request.getKeyword());
        assertEquals(smile, request.getMachineName());

        assertNotNull(request.getClone());
    }
}