package jp.co.canon.ckbs.eec.servicemanager.service;

import lombok.Getter;
import lombok.Setter;

public class SystemInfo {
    @Getter @Setter
    String name;
    @Getter @Setter
    String host;
    @Getter @Setter
    ContainerInfo[] containers;
    @Getter @Setter
    String volumeUsed;
    @Getter @Setter
    String volumeTotal;
}
