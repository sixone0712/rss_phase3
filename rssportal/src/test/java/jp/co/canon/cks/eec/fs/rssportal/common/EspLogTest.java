package jp.co.canon.cks.eec.fs.rssportal.common;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EspLogTest {

    @Test
    void test() {
//        TedLog log = new TedLog();

        EspLog log2 = new EspLog("======= start");
        log2.info("555555555555555", LogType.control);
        System.out.println("======= done");


    }

}