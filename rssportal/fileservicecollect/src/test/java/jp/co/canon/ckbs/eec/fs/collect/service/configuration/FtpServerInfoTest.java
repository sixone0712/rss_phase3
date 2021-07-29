package jp.co.canon.ckbs.eec.fs.collect.service.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FtpServerInfoTest {
    @Test
    void test_001(){
        FtpServerInfo serverInfo = new FtpServerInfo();
        serverInfo.setPort(22001);
        Assertions.assertEquals(22001, serverInfo.getPort());

        serverInfo.setHost("10.1.36.118:AA");

        serverInfo.setFtpmode("active");
        Assertions.assertEquals("active", serverInfo.getFtpmode());
    }
}
