package jp.co.canon.ckbs.eec.fs.collect.service.vftp;

import jp.co.canon.ckbs.eec.fs.collect.model.*;
import jp.co.canon.ckbs.eec.service.DownloadStatusCallback;
import jp.co.canon.ckbs.eec.service.GetFtpCommand;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

@Slf4j
public class VFtpCompatDownloadExecutorThread implements Runnable{
    BlockingDeque<VFtpCompatDownloadRequestEx> mainQueue;
    boolean stop = false;

    CompatDownloadFileRepository downloadFileRepository;
    File workingDir;
    File downloadRoot;

    @Getter @Setter
    VFtpCompatDownloadRequest currentRequest;

    GetFtpCommand command = null;

    public VFtpCompatDownloadExecutorThread(BlockingDeque<VFtpCompatDownloadRequestEx> queue,
                                            CompatDownloadFileRepository downloadFileRepository,
                                            File downloadRoot,
                                            File workingDir){
        this.mainQueue = queue;
        this.downloadFileRepository = downloadFileRepository;
        this.workingDir = workingDir;
        this.downloadRoot = downloadRoot;
    }

    public final void stop(){
        stop = true;
    }

    VFtpCompatDownloadRequestEx dequeue() throws InterruptedException {
        return mainQueue.poll(100, TimeUnit.MILLISECONDS);
    }

    File createFileNameList(VFtpCompatDownloadRequest request) throws Exception {
        File file = null;
        BufferedWriter out = null;
        try {
            file = File.createTempFile("FILE", ".LST", workingDir);
            if (log.isTraceEnabled()){
                log.trace("create filename list file({}) for {}", file.getPath(), request.getRequestNo());
            }
            out = new BufferedWriter(new FileWriter(file));
            out.write(request.getFile().getName());
            out.newLine();
            return file;
        } catch (IOException e) {
            throw new Exception("Failed in making file(" + file.getPath() + ").", e);
        } finally {
            if (out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    throw new Exception("Failed in the close processing of file(" + file.getPath() + ").", e);
                }
            }
        }
    }

    void callCollectCommand(VFtpCompatDownloadRequest request,
                            String host,
                            String user,
                            String password){
        request.setStatus(FtpRequest.Status.EXECUTING);
        File requestDownloadDir = new File(downloadRoot, request.getRequestNo());
        command = new GetFtpCommand();

        try {
            File file = createFileNameList(request);

            command.execute(host,
                    22001,
                    "passive",
                    user,
                    password,
                    "/VROOT/COMPAT/Optional",
                    null,
                    requestDownloadDir.getAbsolutePath(),
                    file.getAbsolutePath(),
                    request.isArchive(),
                    request.getArchiveFileName(),
                    new DownloadStatusCallback() {
                        @Override
                        public void downloadStart(String fileName) {

                        }

                        @Override
                        public void downloadProgress(String fileName, long fileSize) {
                            request.downloadProgress(fileName, fileSize);
                        }

                        @Override
                        public void downloadCompleted(String fileName) {
                            request.downloadCompleted(fileName);
                        }

                        @Override
                        public void archiveCompleted(String archiveFileName, long fileSize) {

                        }
                    });
            request.setStatus(FtpRequest.Status.EXECUTED);
        } catch (Exception e){
            log.error("exception occurred({}).", e.getMessage());
            request.setStatus(FtpRequest.Status.ERROR);
        } finally {
            log.trace("process end ({})", request.getRequestNo());
        }
    }

    void executeRequest(VFtpCompatDownloadRequestEx requestEx){
        VFtpCompatDownloadRequest request = requestEx.getRequest();
        if (request == null){
            return;
        }
        if (request.getStatus() == FtpRequest.Status.CANCEL) {
            return;
        }
        setCurrentRequest(request);

        callCollectCommand(request, requestEx.getHost(), requestEx.getUser(), requestEx.getPassword());

        request.setCompletedTime(System.currentTimeMillis());
        if (request.getStatus() == FtpRequest.Status.EXECUTING) {
            request.setStatus(FtpRequest.Status.EXECUTED);
        }
        try {
            downloadFileRepository.writeRequest(request);
            downloadFileRepository.endRequestExecution(request);
        } catch (Exception e){

        }
        setCurrentRequest(null);
    }

    void executeOneRequest(){
        try {
            VFtpCompatDownloadRequestEx requestEx = dequeue();
            if (requestEx != null) {
                executeRequest(requestEx);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean stopRequestCommand(String requestNo) {
        VFtpCompatDownloadRequest request = getCurrentRequest();
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
