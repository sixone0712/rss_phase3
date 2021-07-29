package jp.co.canon.cks.eec.fs.rssportal.common;

import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileLogTest {

    @Test
    void test1() {
        File file = Paths.get("test", "test-123.log").toFile();
        FileLog fileLog = new FileLog(file, "test123");
        Logger log = fileLog.getLogger();
        log.info("12401-02481-02481-0248-1024");
    }

    @Test
    void test2() {
        File file = Paths.get("test", "test-123.log").toFile();
        FileLog fileLog = new FileLog(file, "test123");
    }

    @Test
    void test3() throws InterruptedException {
        // Test rolling file appender

        File file = Paths.get("test", "rolling.log").toFile();
        FileLog fileLog = new FileLog(file, "rolling", true);

        int i=0;

        while(++i<300) {
            fileLog.info("rolling file logger test!!!");
            Thread.sleep(10000);


        }
    }

    @Test
    void test4() {
        File file = Paths.get("test", "builder.log").toFile();
        FileLog log = new FileLog.Builder().name("builder-test")
                .useRolling(true).file(file).build();
        log.info("hahahahaahahahah");
    }

}