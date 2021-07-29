package jp.co.canon.ckbs.eec.fs.collect.service.vftp;

import jp.co.canon.ckbs.eec.fs.collect.model.VFtpSssListRequest;
import org.junit.jupiter.api.Test;

public class SssListRequestQueueTest {
    @Test
    void test_001(){
        SssListRequestQueue queue = new SssListRequestQueue();
        queue.add(new VFtpSssListRequest());
        queue.pop();
    }
}
