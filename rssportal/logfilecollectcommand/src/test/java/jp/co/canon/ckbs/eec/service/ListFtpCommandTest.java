package jp.co.canon.ckbs.eec.service;

import org.junit.jupiter.api.Test;

public class ListFtpCommandTest {
    String ftpHost = "10.1.31.242";

    @Test
    void test_001(){
        ListFtpCommand command = new ListFtpCommand();

        command.execute(ftpHost,
                22001,
                "passive",
                "ckbs",
                "ckbs",
                "/VROOT/SSS/Optional",
                "IP_AS_RAW-20201130_000000-20201130_235959"
        );
    }
}
