package jp.co.canon.ckbs.eec.fs.collect.action;

import jp.co.canon.ckbs.eec.fs.collect.model.*;
import jp.co.canon.ckbs.eec.service.DownloadStatusCallback;
import jp.co.canon.ckbs.eec.service.command.GetCommand;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FtpCommandExecutorThread implements Runnable{
    BlockingQueue<FtpDownloadRequestEx> mainQueue;
    boolean stop = false;

    FtpDownloadFileRepository downloadFileRepository;
    File workingDir;
    int niceVal;

    @Getter @Setter
    FtpDownloadRequest currentRequest;

    GetCommand command = null;

    public FtpCommandExecutorThread(BlockingQueue<FtpDownloadRequestEx> queue, FtpDownloadFileRepository downloadFileRepository, File workingDir, int niceVal){
        this.mainQueue = queue;
        this.downloadFileRepository = downloadFileRepository;
        this.workingDir = workingDir;
        this.niceVal = niceVal;
    }

    public final void stop(){
        stop = true;
    }

    FtpDownloadRequestEx dequeue() throws InterruptedException {
        return mainQueue.poll(100, TimeUnit.MILLISECONDS);
    }

    File createFileNameList(FtpDownloadRequest request) throws Exception {
        File file = null;
        BufferedWriter out = null;
        try {
            file = File.createTempFile("FILE", ".LST", workingDir);
            out = new BufferedWriter(new FileWriter(file));
            for (RequestFileInfo fileInfo : request.getFileInfos()){
                out.write(fileInfo.getName());
                out.newLine();
            }
            return file;
        } catch (IOException e) {
            log.error("failed to making file({}).", file.getPath());
            throw new Exception("Failed in making file(" + file.getPath() + ").", e);
        } finally {
            if (out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("failed to close buffered writer({}).", file.getPath());
                    throw new Exception("Failed in the close processing of file(" + file.getPath() + ").", e);
                }
            }
        }
    }

    void callCollectCommand(FtpDownloadRequest request, String host, String user, String password) throws Exception{

        command = new GetCommand();

        File file = createFileNameList(request);
        File requestDownloadDir = new File("/CANON/LOG/downloads", request.getDirectory());

        log.info("request execute started : {}", request.getRequestNo());
        boolean result = command.execute(host, "passive", user, password, file.getAbsolutePath(), requestDownloadDir.getAbsolutePath(),
                request.isArchive(),
                request.getArchiveFileName(),
                4,
                true,
                new DownloadStatusCallback() {
                    @Override
                    public void downloadStart(String fileName) {

                    }

                    @Override
                    public void downloadProgress(String fileName, long fileSize) {
                        request.fileDownloadProgress(fileName, fileSize);
                    }

                    @Override
                    public void downloadCompleted(String fileName) {
                        log.info("downloadCompleted {}", fileName);
                        request.fileDownloadCompleted(fileName);
                    }

                    @Override
                    public void archiveCompleted(String archiveFileName, long fileSize) {
                        log.info("archiveCompleted {} {}", archiveFileName, fileSize);
                        request.setArchiveFileSize(fileSize);
                    }
                });
        file.delete();
        if (!result){
            log.info("request execute completed with error : {}", request.getRequestNo());
            request.setStatus(FtpRequest.Status.ERROR);
        } else {
            log.info("request execute completed with success : {}", request.getRequestNo());
            request.setStatus(FtpRequest.Status.EXECUTED);
        }
    }

    void executeRequest(FtpDownloadRequestEx requestEx){
        FtpDownloadRequest request = requestEx.getRequest();
        if (request == null) {
            return;
        }
        if (request.getStatus() == FtpRequest.Status.CANCEL) {
            return;
        }
        request.setStatus(FtpRequest.Status.EXECUTING);
        setCurrentRequest(request);

        try {
            callCollectCommand(request, requestEx.getHost(), requestEx.getUser(), requestEx.getPassword());
        } catch (Exception e){
            request.setStatus(FtpRequest.Status.ERROR);
        }

        request.setCompletedTime(System.currentTimeMillis());
        if (request.getStatus() == FtpRequest.Status.EXECUTING) {
            request.setStatus(FtpRequest.Status.EXECUTED);
        }

        try {
            downloadFileRepository.writeRequest(request);
            downloadFileRepository.endRequestExecution(request);
        } catch (Exception e){
            e.printStackTrace();
        }

        setCurrentRequest(null);
    }

    void executeOneRequest(){
        try {
            FtpDownloadRequestEx requestEx = dequeue();
            if (requestEx != null) {
                executeRequest(requestEx);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean stopRequestCommand(String requestNo){
        FtpDownloadRequest request = getCurrentRequest();
        if (request == null){
            return false;
        }
        if (!request.getRequestNo().equals(requestNo)){
            return false;
        }
        if (request.getStatus() == FtpRequest.Status.EXECUTING) {
            request.setStatus(FtpDownloadRequest.Status.CANCEL);
            if (command != null){
                command.stopDownload();
            }
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        while(!stop){
            executeOneRequest();
        }
    }
}
