package jp.co.canon.ckbs.eec.service;
import jp.co.canon.ckbs.eec.service.command.Configuration;
import jp.co.canon.ckbs.eec.service.command.DownloadInfo;
import jp.co.canon.ckbs.eec.service.command.FtpFileConnection;
import jp.co.canon.ckbs.eec.service.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class GetFtpCommandExecuteThread extends Thread{
    FtpServerInfo serverInfo;

    String rootDir;
    String directory;
    String downloadDirectory;

    FileInfoQueue fileQueue;

    String commandInfoString;

    int downloadedCount = 0;

    boolean exitWithError = false;

    DownloadStatusCallback callback;

    DownloadInfo downloadInfo;

    long total_readed = 0;

    StopChecker stopChecker;

    public GetFtpCommandExecuteThread(FtpServerInfo serverInfo,
                                      String rootDir,
                                      String directory,
                                      String downloadDirectory,
                                      FileInfoQueue fileQueue,
                                      DownloadInfo downloadInfo,
                                      StopChecker stopChecker,
                                      DownloadStatusCallback callback){
        this.serverInfo = serverInfo;
        this.rootDir = rootDir;
        this.directory = directory;
        this.downloadDirectory = downloadDirectory;
        this.fileQueue = fileQueue;

        commandInfoString = createCommandInfoString();
        this.downloadInfo = downloadInfo;
        this.stopChecker = stopChecker;
        this.callback = callback;
    }

    public boolean isExitWithError(){
        return exitWithError;
    }

    String createCommandInfoString(){
        return String.format("(%s, %s, %d, %s, %s, %s, %s)", "get", serverInfo.host, serverInfo.port, serverInfo.ftpmode, this.rootDir, this.directory, this.downloadDirectory);
    }

    @Override
    public void run() {
        Configuration configuration = new Configuration();
        configuration.setScheme("ftp");
        configuration.setHost(this.serverInfo.host);
        configuration.setMode("passive");
        configuration.setPort(this.serverInfo.port);
        configuration.setUser(this.serverInfo.user);
        configuration.setPassword(this.serverInfo.password);
        configuration.setPurpose("download");
        FtpFileConnection connection = null;

        FileInfo fileInfo = fileQueue.poll();
        while(fileInfo != null){
            if (stopChecker.isStopped()){
                break;
            }
            if (connection == null) {
                connection = new FtpFileConnection();
                boolean connected = connection.connect(configuration);
                if (!connected){
                    exitWithError = true;
                    break;
                }
                try {
                    if (!connection.changeDirectory(this.rootDir)) {
                        exitWithError = true;
                        break;
                    }
                    if (this.directory != null) {
                        if (!connection.changeDirectory(this.directory)) {
                            exitWithError = true;
                            break;
                        }
                    }
                } catch (ConnectionClosedException e){
                    log.error("ConnectionClosedException : {}", e.getMessage());
                    log.error("Connection is prematurely closed, try reconnect..");
                    connection.disconnect();
                    connection = null;
                    continue;
                }
            }
            try {
                connection.downloadFile(fileInfo.getFilename(), this.downloadDirectory, fileInfo.getFilename(), this.callback, stopChecker);
                downloadInfo.increaseDownloadCount();
            } catch (ServerBusyWhenDownload e) {
                log.info(e.getMessage());
                fileQueue.push(fileInfo);
                connection.disconnect();
                connection = null;
            } catch (ServerErrorWhenDownload e) {
                if (fileInfo.getRetryCount() < 3) {
                    log.info(e.getMessage());
                    fileInfo.increaseRetryCount();
                    fileQueue.push(fileInfo);
                    connection.disconnect();
                    connection = null;
                } else {
                    log.error(e.getMessage());
                    exitWithError = true;
                    break;
                }
            } catch (ProcessStoppedException e){
                exitWithError = true;
                break;
            } catch (DataConnectionLengthZero e){
                log.error(e.getMessage());
                connection.disconnect();
                connection = null;
            } catch (IOException e){
                log.error(e.getMessage());
                stopChecker.setStopped();
                exitWithError = true;
                break;
            } finally {
                fileInfo = fileQueue.poll();
            }
        }
        if (connection != null){
            connection.disconnect();
        }
    }
}
