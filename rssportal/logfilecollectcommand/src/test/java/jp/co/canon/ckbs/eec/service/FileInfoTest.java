package jp.co.canon.ckbs.eec.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FileInfoTest {
    @Test
    void test_001(){
        FileInfo fileInfo = new FileInfo("abcde.txt", "abcde.txt");
        Assertions.assertEquals("abcde.txt", fileInfo.getFilename());

        Assertions.assertEquals(0, fileInfo.getRetryCount());
        fileInfo.increaseRetryCount();

        Assertions.assertEquals(1, fileInfo.getRetryCount());
    }
}
