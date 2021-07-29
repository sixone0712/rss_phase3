package jp.co.canon.ckbs.eec.service.command;

import jp.co.canon.ckbs.eec.service.DownloadStatusCallback;
import jp.co.canon.ckbs.eec.service.StopChecker;
import jp.co.canon.ckbs.eec.service.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class FtpFileConnection extends FileConnection implements Closeable{
    FTPClient ftpClient = null;
    boolean passiveMode = true;
    long total_read = 0;
    Configuration configuration;

    static int MAX_CONNECT_RETRY = 20;

    private boolean isForChecking() {
        return configuration!=null && configuration.purpose!=null && configuration.purpose.equals("check");
    }

    @Override
    public boolean connect(Configuration configuration) {
        this.configuration = configuration;
        if (configuration.port == -1){
            configuration.port = 21;
        }
        if(!isForChecking()) {
            log.info("host : {}:{} mode={} purpose={}", configuration.host, configuration.port, configuration.mode,
                    configuration.purpose != null ? configuration.purpose : "none");
        }
        if (configuration.mode.equals("active")){
            passiveMode = false;
        }
        int connectRetryCount = 0;
        if(configuration.purpose!=null && configuration.purpose.equals("check")) {
            connectRetryCount = MAX_CONNECT_RETRY - 3;
        }

        while (ftpClient == null) {
            ftpClient = new FTPClient();
            try {
                ftpClient.setControlKeepAliveTimeout(30);
                ftpClient.setConnectTimeout(3000);
                ftpClient.connect(configuration.host, configuration.port);

                int replyCode = ftpClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(replyCode)) {
                    ftpClient.disconnect();
                    ftpClient = null;
                    connectRetryCount++;
                    if (connectRetryCount > MAX_CONNECT_RETRY){
                        log.error("ftp reply is not positive completion {} retry count over", replyCode);
                        return false;
                    }
                    log.error("ftp reply is not positive completion {} retry... ", replyCode);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                    continue;
                }
            } catch (FTPConnectionClosedException e) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ioException) {
                }
                ftpClient = null;
                connectRetryCount++;
                if (connectRetryCount > MAX_CONNECT_RETRY){
                    log.error("connect failed by FTPConnectionClosedException {} retry count over", e.getMessage());
                    return false;
                }
                log.error("connect failed by FTPConnectionClosedException {} retry...", e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                continue;
            } catch (IOException e) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ioException) {

                }
                ftpClient = null;
                if(e.getMessage().contains("Too many client")) {
                    connectRetryCount++;
                    if (connectRetryCount > MAX_CONNECT_RETRY){
                        log.error("login failed by exception {}. retry count over", e.getMessage());
                        return false;
                    }
                    log.error("login failed by exception {}. retry...", e.getMessage());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                    continue;
                }
                if(!isForChecking()) {
                    log.error("{}:{} login failed by exception {}", configuration.host, configuration.rootPath,
                            e.getMessage());
                }
                return false;
            }

            try {
                boolean logined = ftpClient.login(configuration.user, configuration.password);
                if (!logined) {
                    log.info("login failed. {} {} {} {}", configuration.host, configuration.port, configuration.user, configuration.password);
                    ftpClient.disconnect();
                    ftpClient = null;
                    return false;
                }
            } catch (FTPConnectionClosedException e) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ioException) {
                }
                ftpClient = null;
                connectRetryCount++;
                if (connectRetryCount > MAX_CONNECT_RETRY){
                    log.error("login failed by FTPConnectionClosedException {} retry count over", e.getMessage());
                    return false;
                }
                log.error("login failed by FTPConnectionClosedException {} retry...", e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                continue;
            } catch (IOException e){
                try {
                    ftpClient.disconnect();
                } catch (IOException ioException) {

                }
                ftpClient = null;
                if(e.getMessage().contains("Too many client")) {
                    connectRetryCount++;
                    if (connectRetryCount > MAX_CONNECT_RETRY){
                        log.error("login failed by FTPConnectionClosedException {} retry count over", e.getMessage());
                        return false;
                    }
                    log.error("login failed by exception {}. retry...", e.getMessage());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                    continue;
                }
                if(!isForChecking()) {
                    log.error("login failed by exception.. {}", e.getMessage());
                }
                return false;
            }
            Thread.yield();
        }
        ftpClient.setBufferSize(1024*1024);
        if(!isForChecking()) {
            log.info("login success. {} {} {} {}", configuration.host, configuration.port, configuration.user, configuration.password);
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        if (ftpClient != null){
            disconnect();
        }
    }

    @Override
    public void disconnect() {
        if(!isForChecking()) {
            log.info("disconnect");
        }
        try {
            if (ftpClient != null) {
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ftpClient = null;
    }

    @Override
    public boolean changeDirectory(String directory)
            throws ConnectionClosedException{
        //log.info("changeDirectory({})", directory);
        try {
            boolean moved = ftpClient.changeWorkingDirectory(directory);
            if (!moved){
                log.error("changeDirectory({}) failed", directory);
            }
            return moved;
        } catch (FTPConnectionClosedException e){
            throw new ConnectionClosedException(String.format("Connection Closed while changeDirectory(%s)", directory));
        } catch (IOException e) {
            log.error("changeDirectory({}) failed", directory);
            return false;
        }
    }

    void applyFtpMode(){
        if (passiveMode){
            ftpClient.enterLocalPassiveMode();
        } else {
            ftpClient.enterLocalActiveMode();
        }
    }

    @Override
    public LogFileInfo[] listFiles() throws FtpConnectionException {
        try {
            applyFtpMode();
            FTPFile[] files = ftpClient.listFiles();
            ArrayList<LogFileInfo> logFileInfoArrayList = new ArrayList<>();
            for(FTPFile file : files){
                LogFileInfo info = new LogFileInfo(file.getName(), file.getSize(), file.getTimestamp(), file.isFile());
                logFileInfoArrayList.add(info);
            }
            return logFileInfoArrayList.toArray(new LogFileInfo[0]);
        } catch (IOException e) {
            //return new LogFileInfo[0];
            log.error("listFiles() exception occurs. error={}", e.getMessage());
            throw new FtpConnectionException("failed to list files");
        }
    }

    @Override
    public LogFileInfo[] listFiles(String directory) throws FtpConnectionException {
        try {
            applyFtpMode();
            FTPFile[] files = ftpClient.listFiles(directory);
            List<LogFileInfo> logFileInfoList = new ArrayList<>();
            for(FTPFile file : files) {
                LogFileInfo info = new LogFileInfo(file.getName(), file.getSize(), file.getTimestamp(), file.isFile());
                logFileInfoList.add(info);
            }
            return logFileInfoList.toArray(new LogFileInfo[0]);
        } catch (IOException e) {
            //return new LogFileInfo[0];
            log.error("listFiles({}) exception occurs. error={}", directory, e.getMessage());
            throw new FtpConnectionException("failed to list files");
        }
    }

    @Override
    public InputStream getInputStream(String fileName) throws IOException {
        applyFtpMode();
        InputStream inputStream;

        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
        inputStream = ftpClient.retrieveFileStream(fileName);

        return inputStream;
    }

    @Override
    public void completePendingCommand(){
        try {
            ftpClient.completePendingCommand();
        } catch (IOException e) {
            log.error("Exception : {}", e.getMessage());
        }
    }

    public void downloadTarget(String remoteFileName,
                               String saveDir,
                               String saveFileName,
                               DownloadStatusCallback callback,
                               StopChecker stopChecker
    ) throws ServerBusyWhenDownload, ServerErrorWhenDownload, DataConnectionLengthZero, IOException, ProcessStoppedException {

        LogFileInfo[] files;
        try {
            files = listFiles(remoteFileName);
        } catch (FtpConnectionException e) {
            log.error("failed to list target files");
            throw new ServerErrorWhenDownload("failed to list target files");
        }
        log.info("downloadTarget {} files={}", remoteFileName, files.length);

        LogFileInfo targetFileInfo = null;
        for(LogFileInfo file: files) {
            if(remoteFileName.endsWith(file.getName())) {
                targetFileInfo = file;
                break;
            }
        }

        if(targetFileInfo!=null && targetFileInfo.getIsFile()) {
            downloadFile(remoteFileName, saveDir, saveFileName, callback, stopChecker);
        } else {
            log.info("target {} is a directory", remoteFileName);
            downloadDirectory(remoteFileName, saveDir, callback, stopChecker);
        }
    }

    public void downloadDirectory(String directoryName,
                                  String saveDir,
                                  DownloadStatusCallback callback,
                                  StopChecker stopChecker) throws IOException, ServerErrorWhenDownload, ServerBusyWhenDownload, DataConnectionLengthZero, ProcessStoppedException {

        File destDir = new File(saveDir, directoryName);
        destDir.mkdirs();
        log.info("destination: {}", destDir.toString());

        downloadDirectoryRecursive(directoryName, destDir.toString(), callback, stopChecker);
    }

    private void downloadDirectoryRecursive(String source, String dest, DownloadStatusCallback callback,
                                            StopChecker stopChecker)
            throws IOException, ServerBusyWhenDownload, ServerErrorWhenDownload, DataConnectionLengthZero, ProcessStoppedException {

        FTPFile[] files = ftpClient.listFiles(source);

        if(files.length==0) {
            log.info("empty directory {}", source);
            callback.downloadCompleted(source);
            return;
        }

        total_read = 0;

        for(FTPFile file: files) {
            String fileName = source+"/"+file.getName();
            if(file.isDirectory()) {
                downloadDirectoryRecursive(fileName, fileName, callback, stopChecker);
            } else {
                InputStream inputStream = null;
                try {
                    inputStream = getInputStream(fileName);
                    if(inputStream==null) {
                        int replyCode = ftpClient.getReplyCode();
                        if (replyCode / 100 == 4){
                            throw new ServerBusyWhenDownload(
                                    String.format("FTP Server Busy(Status:%d, File:%s)", replyCode, fileName));
                        }
                        throw new ServerErrorWhenDownload(
                                String.format("FTP Server reply Error Status(Status:%d, File:%s)", replyCode, fileName));
                    }

                    byte[] buffer = new byte[8192];

                    Timer progressTimer = new Timer();
                    progressTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            callback.downloadProgress(source, total_read);
                            /*try {
                                ftpClient.noop();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }*/
                        }
                    }, 1000, 1000);

                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(new File(dest, file.getName()));
                        callback.downloadStart(source);
                        int readed = 0;
                        while((readed=inputStream.read(buffer))>0) {
                            if (stopChecker.isStopped()){
                                throw new ProcessStoppedException("Stopped");
                            }
                            outputStream.write(buffer, 0, readed);
                            total_read += readed;
                            Thread.yield();
                        }
                        callback.downloadProgress(source, total_read);
                    } finally {
                        progressTimer.cancel();
                        callback.downloadCompleted(source);
                        if(outputStream!=null) {
                            outputStream.close();
                        }
                    }
                } catch (IOException e) {
                    log.error("file not found exception occurs");
                    throw e;
                } finally {
                    if(inputStream!=null) {
                        inputStream.close();
                        completePendingCommand();
                    }
                    if(total_read==0){
                        // In this case, it should not throw an exception.
                        // If an exception occurs, all recursive works are stopped.
                        //throw new DataConnectionLengthZero(String.format("data Connection zero(%s)", fileName));
                        log.error("zero byte transferred. file={}", file.getName());
                    }
                }
            }
        }
    }

    public void downloadFile(String remoteFileName,
                             String saveDir,
                             String saveFileName,
                             DownloadStatusCallback callback,
                             StopChecker stopChecker
                             )
            throws
            ServerBusyWhenDownload,
            ServerErrorWhenDownload,
            DataConnectionLengthZero,
            IOException,
            ProcessStoppedException {

        File destDir = new File(saveDir);

        if (saveFileName.contains("/")){
            int idx = saveFileName.lastIndexOf("/");
            String dir = saveFileName.substring(0, idx);
            destDir = new File(destDir, dir);
        }

        destDir.mkdirs();

        InputStream inputStream = null;
        try {
            inputStream = getInputStream(remoteFileName);
            if (inputStream == null){
                int replyCode = ftpClient.getReplyCode();
                if (replyCode / 100 == 4){
                    throw new ServerBusyWhenDownload(String.format("FTP Server Busy(Status:%d)", replyCode));
                }
                throw new ServerErrorWhenDownload(String.format("FTP Server reply Error Status(Status:%d)", replyCode));
            }

            total_read = 0;
            byte[] buffer = new byte[8192];

            Timer progressTimer = new Timer();
            progressTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    callback.downloadProgress(remoteFileName, total_read);
                    /*log.info("progress read={}", total_read);
                    try {
                        ftpClient.noop();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                }
            }, 1000, 1000);

            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(new File(saveDir, saveFileName));
                callback.downloadStart(remoteFileName);
                int read = 0;
                while ((read = inputStream.read(buffer)) > 0) {
                    if (stopChecker.isStopped()){
                        throw new ProcessStoppedException("Stopped");
                    }
                    outputStream.write(buffer, 0, read);
                    total_read += read;
                    Thread.yield();
                }
                callback.downloadProgress(remoteFileName, total_read);
                callback.downloadCompleted(remoteFileName);
            } catch(FileNotFoundException e){
                log.error("file not found exception occurs");
                throw e;
            } finally {
                progressTimer.cancel();
                if(outputStream!=null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            log.error("io exception msg={}", e.getMessage());
            throw e;
        } finally {
            if(total_read>0) {
                if(inputStream!=null) {
                    inputStream.close();
                    completePendingCommand();
                } else {
                    throw new DataConnectionLengthZero(String.format("data Connection zero(%s)", remoteFileName));
                }
            }
        }
    }
}
