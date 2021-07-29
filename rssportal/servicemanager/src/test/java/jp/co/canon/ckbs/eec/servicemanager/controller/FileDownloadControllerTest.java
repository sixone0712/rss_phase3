package jp.co.canon.ckbs.eec.servicemanager.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FileDownloadControllerTest {
    @Autowired
    FileDownloadController controller;

    @Test
    void test_001(){
        controller.getFileList("ESP");

        controller.getFileList("OTS_1");

        controller.getFileList("OTS01");
    }
}
