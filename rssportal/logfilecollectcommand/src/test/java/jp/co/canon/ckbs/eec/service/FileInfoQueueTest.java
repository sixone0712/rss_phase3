package jp.co.canon.ckbs.eec.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FileInfoQueueTest {
    @Test
    void test_001(){
        FileInfoQueue queue = new FileInfoQueue();

        FileInfo fileInfo = new FileInfo("abcde.txt", "abcde.txt");
        queue.push(fileInfo);

        Assertions.assertEquals(1, queue.size());

        FileInfo fileInfo2 = queue.poll();
        Assertions.assertEquals(fileInfo, fileInfo2);
    }
}
