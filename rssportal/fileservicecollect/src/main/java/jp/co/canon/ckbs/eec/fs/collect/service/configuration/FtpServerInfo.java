package jp.co.canon.ckbs.eec.fs.collect.service.configuration;

import lombok.Getter;
import lombok.Setter;

public class FtpServerInfo {
    @Getter @Setter
    String host;

    @Getter @Setter
    int port = 21;

    @Getter @Setter
    String user;

    @Getter @Setter
    String password;

    @Setter
    String ftpmode;

    public String getFtpmode(){
        if (ftpmode != null && ftpmode.compareToIgnoreCase("active") == 0){
            return "active";
        }
        return "passive";
    }
}
