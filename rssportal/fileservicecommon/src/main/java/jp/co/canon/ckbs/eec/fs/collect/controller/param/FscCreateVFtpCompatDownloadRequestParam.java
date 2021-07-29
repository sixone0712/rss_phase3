package jp.co.canon.ckbs.eec.fs.collect.controller.param;

import lombok.Getter;
import lombok.Setter;

public class FscCreateVFtpCompatDownloadRequestParam {
    @Getter @Setter
    String machine;

    @Getter @Setter
    String filename;

    @Getter @Setter
    boolean archive;

    @Getter @Setter
    String host;

    @Getter @Setter
    String user;

    @Getter @Setter
    String password;
}
