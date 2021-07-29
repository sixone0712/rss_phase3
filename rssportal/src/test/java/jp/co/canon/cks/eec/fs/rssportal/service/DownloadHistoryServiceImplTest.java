package jp.co.canon.cks.eec.fs.rssportal.service;

import jp.co.canon.cks.eec.fs.rssportal.vo.DownloadHistoryVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DownloadHistoryServiceImplTest {

    @Autowired
    private DownloadHistoryService service;

    @Test
    void test() {
        assertNotNull(service);

        assertNotNull(service.getHistoryList());
        assertFalse(service.addDlHistory(new DownloadHistoryVo()));
        DownloadHistoryVo h = new DownloadHistoryVo();
        h.setDl_filename("test:)");
        h.setDl_status("status:)");
        h.setDl_type("type:)");
        h.setDl_user("me:)");
        h.setDl_date(new Date());
        assertTrue(service.addDlHistory(h));
    }


}