package jp.co.canon.ckbs.eec.servicemanager.controller;

import jp.co.canon.ckbs.eec.servicemanager.service.SystemInfo;
import lombok.Getter;
import lombok.Setter;

public class SystemInfoListResponse {
    @Getter @Setter
    SystemInfo[] list;
}
