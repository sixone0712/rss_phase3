package jp.co.canon.ckbs.eec.service.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServerBusyWhenDownloadTest {
    @Test
    void test_001(){
        ServerBusyWhenDownload e = new ServerBusyWhenDownload("msg");
        Assertions.assertEquals("msg", e.getMessage());
    }
}
