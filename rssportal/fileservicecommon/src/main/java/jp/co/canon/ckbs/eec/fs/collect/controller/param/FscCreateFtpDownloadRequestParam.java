package jp.co.canon.ckbs.eec.fs.collect.controller.param;

import lombok.Getter;
import lombok.Setter;

public class FscCreateFtpDownloadRequestParam {
    @Getter @Setter
    String machine;

    @Getter @Setter
    String category;

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
