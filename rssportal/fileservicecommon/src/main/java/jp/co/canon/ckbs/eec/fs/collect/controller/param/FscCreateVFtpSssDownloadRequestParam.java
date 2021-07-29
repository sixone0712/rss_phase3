package jp.co.canon.ckbs.eec.fs.collect.controller.param;

import lombok.Getter;
import lombok.Setter;

public class FscCreateVFtpSssDownloadRequestParam {
    @Getter @Setter
    String machine;

    @Getter @Setter
    String directory;

    @Getter @Setter
    String[] fileList;

    @Getter @Setter
    boolean archive;

    @Getter @Setter
    String host;

    @Getter @Setter
    String user;

    @Getter @Setter
    String password;
}
