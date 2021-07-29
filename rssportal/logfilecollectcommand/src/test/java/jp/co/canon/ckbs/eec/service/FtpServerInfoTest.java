package jp.co.canon.ckbs.eec.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FtpServerInfoTest {
    @Test
    void test_001(){
        FtpServerInfo ftpServerInfo = new FtpServerInfo();
        Assertions.assertNotNull(ftpServerInfo);
    }
}
