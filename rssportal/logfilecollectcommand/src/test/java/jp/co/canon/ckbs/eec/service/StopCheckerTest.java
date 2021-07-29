package jp.co.canon.ckbs.eec.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StopCheckerTest {
    @Test
    void test_001(){
        StopChecker stopChecker = new StopChecker();
        stopChecker.setStopped();
        Assertions.assertTrue(stopChecker.isStopped());
    }
}
