package jp.co.canon.ckbs.eec.service;

import jp.co.canon.ckbs.eec.service.command.Configuration;
import jp.co.canon.ckbs.eec.service.command.FtpFileConnection;
import jp.co.canon.ckbs.eec.service.command.LogFileInfo;
import jp.co.canon.ckbs.eec.service.exception.ConnectionClosedException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class ListFtpCommand extends BaseFtpCommand {
    String rootDir;
    String destDir;

    String commandInfoString;

    String createCommandInfoString(){
        return String.format("(%s, %s, %d, %s, %s, %s)", "list", this.host, this.port, this.ftpmode, this.rootDir, this.destDir);
    }

    LogFileInfo[] listFiles() throws Exception{
        Configuration configuration = new Configuration();
        configuration.setScheme("ftp");
        configuration.setHost(host);
        configuration.setPort(port);
        configuration.setMode("passive");
        configuration.setRootPath(rootDir);
        configuration.setUser(user);
        configuration.setPassword(password);
        configuration.setPurpose("list");

        boolean retry;
        int retryCount = 0;
        do {
            retry = false;
            try {
                try(FtpFileConnection connection = new FtpFileConnection()){
                    boolean connected = connection.connect(configuration);
                    if (connected){
                        boolean rc = connection.changeDirectory(rootDir);
                        if (!rc){
                            throw new Exception("cannot move directory");
                        }
                        rc = connection.changeDirectory(destDir);
                        if (!rc){
                            throw new Exception("cannot move directory");
                        }
                        ArrayList<LogFileInfo> list = new ArrayList<>();
                        LogFileInfo[] rlist = connection.listFiles();
                        for(LogFileInfo info : rlist){
                            if (info.getIsFile()){
                                list.add(info);
                            }
                        }
                        return list.toArray(new LogFileInfo[0]);
                    } else {
                        throw new Exception("Connection Failed");
                    }
                }
            } catch (ConnectionClosedException e){
                retry = true;
                retryCount++;
            }
        } while(retry && retryCount < 5);
        throw new Exception("Connection Failed");
    }

    public LogFileInfo[] execute(String host,
                             int port,
                             String ftpmode,
                             String user,
                             String password,
                             String rootDir,
                             String destDir){
        this.host = host;
        this.port = port;
        this.ftpmode = ftpmode;
        this.user = user;
        this.password = password;
        this.rootDir = rootDir;
        this.destDir = destDir;

        this.commandInfoString = createCommandInfoString();
        try {
            return listFiles();
        }catch (Exception e){
            return new LogFileInfo[0];
        }
    }
}
