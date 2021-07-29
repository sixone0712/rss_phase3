package jp.co.canon.ckbs.eec.service.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServerErrorWhenDownloadTest {
    @Test
    void test_001(){
        ServerErrorWhenDownload e = new ServerErrorWhenDownload("ABC");
        Assertions.assertEquals(e.getMessage(), "ABC");
    }
}
