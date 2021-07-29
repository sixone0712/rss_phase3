package jp.co.canon.cks.eec.fs.rssportal.background.machine;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MachineStatus {

    public enum Type {
        mpa, ots
    }

    private String name;
    private Type type;
    private String ftpStatus;
    private String vFtpStatus;
    private String otsStatus;

    public MachineStatus() {}

    public MachineStatus(String name, Type type, String otsStatus) {
        this.name = name;
        this.type = type;
        this.otsStatus = otsStatus;
    }

    public MachineStatus(String name, Type type, String ftpStatus, String vFtpStatus) {
        this.name = name;
        this.type = type;
        this.ftpStatus = ftpStatus;
        this.vFtpStatus = vFtpStatus;
    }
}
