package jp.co.canon.cks.eec.fs.rssportal.background;

import jp.co.canon.ckbs.eec.fs.collect.model.VFtpSssListRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FtpDownloadRequestFormTest {

    @Test
    void test() {
        FtpDownloadRequestForm form = new FtpDownloadRequestForm("","","","");
        form.addFile("",123, "",  123);
    }
}
