package jp.co.canon.ckbs.eec.servicemanager.service;

import lombok.Getter;
import lombok.Setter;

public class CreateDownloadRequestResult {
    @Getter @Setter
    String requestNo;

    @Getter @Setter
    int errorCode;

    @Getter @Setter
    String errorMessage;
}
