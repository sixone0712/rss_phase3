package jp.co.canon.cks.eec.fs.rssportal.dao;

import jp.co.canon.cks.eec.fs.rssportal.vo.DownloadHistoryVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DownloadHistoryDaoImplTest {

    @Autowired
    private DownloadHistoryDao d;

    @Test
    void test() {
        assertNotNull(d);

        assertNotNull(d.findAll());
        DownloadHistoryVo h = new DownloadHistoryVo();
        assertTrue(d.add(h));
    }

}