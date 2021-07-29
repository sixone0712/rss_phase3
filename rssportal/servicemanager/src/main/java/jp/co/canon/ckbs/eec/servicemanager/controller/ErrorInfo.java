package jp.co.canon.ckbs.eec.servicemanager.controller;

import lombok.Getter;
import lombok.Setter;

public class ErrorInfo {
    @Getter @Setter
    int code;
    @Getter @Setter
    String message;
}
