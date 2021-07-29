package jp.co.canon.ckbs.eec.servicemanager.service;

import lombok.Getter;
import lombok.Setter;

public class DownloadRequestResult {
    @Getter @Setter
    String requestNo;

    @Getter @Setter
    String status;

    @Getter @Setter
    String url;

    @Getter @Setter
    int errorCode;

    @Getter @Setter
    String errorMessage;
}
