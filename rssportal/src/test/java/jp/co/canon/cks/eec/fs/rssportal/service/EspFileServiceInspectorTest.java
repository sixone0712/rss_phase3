package jp.co.canon.cks.eec.fs.rssportal.service;

import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLogFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EspFileServiceInspectorTest {

    private EspLog log = new EspLog(getClass());

    @Autowired
    private FileServiceInspector inspector;

    @Test
    void test1() {
        log.info("===================== start ");
        inspector.checkMachine("MPA1");
        log.info("===================== end ");
    }
}