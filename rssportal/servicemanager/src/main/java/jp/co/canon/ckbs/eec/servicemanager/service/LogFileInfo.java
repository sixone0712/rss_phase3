package jp.co.canon.ckbs.eec.servicemanager.service;

import lombok.Getter;
import lombok.Setter;

public class LogFileInfo {
    @Getter @Setter
    String fileType;
    @Getter @Setter
    String fileName;
    @Getter @Setter
    long fileSize;
}
