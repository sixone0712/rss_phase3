package jp.co.canon.ckbs.eec.servicemanager.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SystemServiceTest {
    @Autowired
    SystemService systemService;

    @Test
    void test_001(){
        systemService.getContainerInfos();
    }

    @Test
    void test_002() {
        systemService.getOtsInfos();
        systemService.getOtsInfo("MPA_1");
        systemService.getSystemInfo();
    }
}
