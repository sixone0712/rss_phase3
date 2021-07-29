package jp.co.canon.ckbs.eec.fs.manage.service.configuration;

import lombok.Data;

@Data
public class Machine {

    String machineName;
    String host;
    String ots;
    String line;
    String ftpUser;
    String ftpPassword;
    String vftpUser;
    String vftpPassword;
    String serialNumber;
    String toolType;
    int port;
    boolean ftpConnected;
    boolean vFtpConnected;

    public Machine(){
    }

    public Machine(String machineName,
                   String host,
                   String ots,
                   String line,
                   String ftpUser,
                   String ftpPassword,
                   String vftpUser,
                   String vftpPassword,
                   String serialNumber,
                   String toolType,
                   int port) {
        this(machineName, host, ots, line, ftpUser, ftpPassword, vftpUser, vftpPassword, serialNumber, toolType,
                port, false, false);
    }

    public Machine(String machineName,
                   String host,
                   String ots,
                   String line,
                   String ftpUser,
                   String ftpPassword,
                   String vftpUser,
                   String vftpPassword,
                   String serialNumber,
                   String toolType,
                   int port,
                   boolean ftpConnected,
                   boolean vFtpConnected) {
        this.machineName = machineName;
        this.host = host;
        this.ots = ots;
        this.line = line;
        this.ftpUser = ftpUser;
        this.ftpPassword = ftpPassword;
        this.vftpUser = vftpUser;
        this.vftpPassword = vftpPassword;
        this.serialNumber = serialNumber;
        this.toolType = toolType;
        this.port = port;
        this.ftpConnected = ftpConnected;
        this.vFtpConnected = vFtpConnected;
    }
}
