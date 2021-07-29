package jp.co.canon.ckbs.eec.service.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataConnectionLengthZeroTest {
    @Test
    void test_001(){
        DataConnectionLengthZero e = new DataConnectionLengthZero("msg");
        Assertions.assertEquals("msg", e.getMessage());
    }
}
