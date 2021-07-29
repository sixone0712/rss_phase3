package jp.co.canon.ckbs.eec.service;

import org.junit.jupiter.api.Test;

public class GetFtpCommandTest {
    String ftpHost = "10.1.31.242";

    @Test
    void test_001(){
        GetFtpCommand command = new GetFtpCommand();
        try {
            command.execute(ftpHost,
                    22001,
                    "passive",
                    "ckbs",
                    "ckbs",
                    "/VROOT/COMPAT/Optional",
                    null,
                    "/TEMP",
                    "/TEMP/AAA.LST",
                    true,
                    "AAA.zip",
                    new DownloadStatusCallback() {
                        @Override
                        public void downloadStart(String fileName) {

                        }

                        @Override
                        public void downloadProgress(String fileName, long fileSize) {

                        }

                        @Override
                        public void downloadCompleted(String fileName) {

                        }

                        @Override
                        public void archiveCompleted(String archiveFileName, long fileSize) {

                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
