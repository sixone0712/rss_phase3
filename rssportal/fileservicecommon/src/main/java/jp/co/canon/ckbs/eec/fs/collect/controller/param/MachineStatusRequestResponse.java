package jp.co.canon.ckbs.eec.fs.collect.controller.param;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MachineStatusRequestResponse {

    private String fab;
    private String ots;
    private String machine;
    private String ftpStatus;
    private String vFtpStatus;

    private int errorCode;
    private String errorMessage;
}
