package jp.co.canon.ckbs.eec.servicemanager.service;

import lombok.Getter;
import lombok.Setter;

public class DownloadRequest {
    @Getter @Setter
    String requestNo;

    @Getter @Setter
    String status;

    @Getter @Setter
    String url;

    @Getter @Setter
    String[] fileNames;
}
