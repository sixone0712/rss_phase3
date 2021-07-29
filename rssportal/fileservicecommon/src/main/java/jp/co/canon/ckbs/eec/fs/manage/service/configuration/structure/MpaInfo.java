package jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure;

import lombok.Getter;
import lombok.Setter;

public class MpaInfo {
    @Getter @Setter
    String name;
    @Getter @Setter
    String host;
    @Getter @Setter
    String ots;
    @Getter @Setter
    String line;
    @Getter @Setter
    String ftpUser;
    @Getter @Setter
    String ftpPassword;
    @Getter @Setter
    String vftpUser;
    @Getter @Setter
    String vftpPassword;
    @Getter @Setter
    String serialNumber;
    @Getter @Setter
    String toolType;
}
