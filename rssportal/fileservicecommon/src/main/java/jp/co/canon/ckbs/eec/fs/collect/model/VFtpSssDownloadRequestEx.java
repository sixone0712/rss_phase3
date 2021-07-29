package jp.co.canon.ckbs.eec.fs.collect.model;

import lombok.Getter;
import lombok.Setter;

public class VFtpSssDownloadRequestEx {
    @Getter @Setter
    VFtpSssDownloadRequest request;
    @Getter @Setter
    String host;
    @Getter @Setter
    String user;
    @Getter @Setter
    String password;
}
