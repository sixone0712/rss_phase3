package jp.co.canon.ckbs.eec.fs.collect.controller.param;

import lombok.Data;

@Data
public class FscMachineStatusRequestParam {

    private String fab;
    private String machine;
    private String host;
    private String ftpUser;
    private String ftpPassword;
    private String vFtpUser;
    private String vFtpPassword;
}
