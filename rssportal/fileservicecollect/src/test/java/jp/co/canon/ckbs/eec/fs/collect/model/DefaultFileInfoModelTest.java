package jp.co.canon.ckbs.eec.fs.collect.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DefaultFileInfoModelTest {
    @Test
    void test_001(){
        DefaultFileInfoModel model = new DefaultFileInfoModel();

        model.setName("ABCDE");
        Assertions.assertEquals("ABCDE", model.getName());

        model.setSize(100);
        Assertions.assertEquals(100, model.getSize());

        model.setType("D");
        Assertions.assertEquals("D", model.getType());

        model.setTimestamp("20200909121212");
        Assertions.assertEquals("20200909121212", model.getTimestamp());

        model.setExists(true);
        Assertions.assertTrue(model.isExists());
    }
}
