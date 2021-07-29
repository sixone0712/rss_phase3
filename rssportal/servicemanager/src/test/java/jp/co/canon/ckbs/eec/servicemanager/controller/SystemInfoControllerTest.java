package jp.co.canon.ckbs.eec.servicemanager.controller;

import jp.co.canon.ckbs.eec.servicemanager.service.LoginInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SystemInfoControllerTest {
    @Autowired
    SystemInfoController controller;

    @Test
    void test_001(){
        controller.getSystemInfo();

        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setId("root");
        loginInfo.setPassword("acbdefg");
        controller.restartSystem("OTS_1", loginInfo);

        controller.restartSystem("OTS01", loginInfo);

        controller.restartSystem("ESP", loginInfo);
    }

    @Test
    void test_002(){
        controller.restartSystemContainers("ESP");

        controller.restartSystemContainers("OTS_1");
    }
}
