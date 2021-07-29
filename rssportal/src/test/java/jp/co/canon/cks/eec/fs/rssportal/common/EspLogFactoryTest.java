package jp.co.canon.cks.eec.fs.rssportal.common;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EspLogFactoryTest {

    private final EspLog log = new EspLog(getClass());

    @Test
    void test1() {
        log.info("start-------------------------------------------");
        FileLog logger = EspLogFactory.getFileLogger("dl123123");
        logger.info("test!!");
        logger.info(false, "put log on file and console!");
        logger.info("exception log", LogType.exception);
        log.info("end-------------------------------------------");
    }

    @Test
    void test2() {
        log.info("start-------------------------------------------");
        FileLog logger = EspLogFactory.getFileLogger("dl22222");
        logger.debug("debug");
        log.info("end-------------------------------------------");
    }

}