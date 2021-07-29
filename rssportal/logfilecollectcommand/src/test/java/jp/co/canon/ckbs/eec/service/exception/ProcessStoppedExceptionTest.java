package jp.co.canon.ckbs.eec.service.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProcessStoppedExceptionTest {
    @Test
    void test_001(){
        ProcessStoppedException e = new ProcessStoppedException("msg");
        Assertions.assertEquals("msg", e.getMessage());
    }
}
