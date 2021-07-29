package jp.co.canon.ckbs.eec.service.command;

import jp.co.canon.ckbs.eec.service.DownloadStatusCallback;
import jp.co.canon.ckbs.eec.service.FileInfo;
import jp.co.canon.ckbs.eec.service.FileInfoQueue;
import jp.co.canon.ckbs.eec.service.StopChecker;
import jp.co.canon.ckbs.eec.service.exception.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class GetCommandExecuteThread extends Thread{
    Configuration configuration;
    FileAccessor fileAccessor;
    FileInfoQueue fileQueue;
    boolean preserveDirStructure;
    String downloadDirectory;
    DownloadInfo downloadInfo;
    FileInfoQueue downloadedQueue;
    StopChecker stopChecker;
    DownloadStatusCallback callback;

    boolean exitWithError = false;
    public GetCommandExecuteThread(Configuration configuration,
                                   FileAccessor fileAccessor,
                                   String downloadDirectory,
                                   FileInfoQueue fileQueue,
                                   boolean preserveStructure,
                                   DownloadInfo downloadInfo,
                                   FileInfoQueue downloadedQueue,
                                   StopChecker stopChecker,
                                   DownloadStatusCallback callback){
        this.configuration = configuration;
        this.fileAccessor = fileAccessor;
        this.downloadDirectory = downloadDirectory;
        this.fileQueue = fileQueue;
        this.preserveDirStructure = preserveStructure;
        this.downloadInfo = downloadInfo;
        this.downloadedQueue = downloadedQueue;
        this.stopChecker = stopChecker;
        this.callback = callback;
    }

    public boolean getExitWithError(){
        return exitWithError;
    }

    @Override
    public void run() {
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
                    if (!connection.changeDirectory(configuration.rootPath)) {
                        exitWithError = true;
                        break;
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
                connection.downloadTarget(fileInfo.getFilename(), this.downloadDirectory, fileInfo.getFilename(), this.callback, stopChecker);
                downloadInfo.increaseDownloadCount();
                downloadedQueue.push(fileInfo);
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
