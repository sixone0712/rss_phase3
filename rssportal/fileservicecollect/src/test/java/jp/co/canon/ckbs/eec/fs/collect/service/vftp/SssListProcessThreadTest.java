package jp.co.canon.ckbs.eec.fs.collect.service.vftp;

import jp.co.canon.ckbs.eec.fs.collect.model.VFtpSssListRequest;
import org.junit.jupiter.api.Test;

public class SssListProcessThreadTest {
    @Test
    void test_001(){
        VFtpSssListRequest request = new VFtpSssListRequest();
        SssListProcessThread th = new SssListProcessThread(request, null, null);

        th.stopExecute();
    }
}
