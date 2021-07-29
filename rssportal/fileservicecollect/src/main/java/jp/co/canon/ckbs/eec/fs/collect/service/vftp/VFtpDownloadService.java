package jp.co.canon.ckbs.eec.fs.collect.service.vftp;

import jp.co.canon.ckbs.eec.fs.collect.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Component
public class VFtpDownloadService {
    @Autowired
    SssDownloadFileRepository sssDownloadFileRepository;
    @Autowired
    CompatDownloadFileRepository compatDownloadFileRepository;

    @Value("${fileservice.collect.workDirectory}")
    String workDirectory;

    @Value("${fileservice.collect.vftp.downloadDirectory}")
    String vftpDownloadDirectory;

    long lastRequestNumber = 0;
    DateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    File workingDir;
    File downloadDir;

    BlockingDeque<VFtpSssDownloadRequestEx> mainQueueSss = new LinkedBlockingDeque<>();
    BlockingDeque<VFtpCompatDownloadRequestEx> mainQueueCompat = new LinkedBlockingDeque<>();

    List<VFtpSssDownloadExecutorThread> vFtpSssDownloadExecutorThreadList = new ArrayList<>();
    List<VFtpCompatDownloadExecutorThread> vFtpCompatDownloadExecutorThreadList = new ArrayList<>();

    @PostConstruct
    private void postConstruct(){
        downloadDir = new File(vftpDownloadDirectory);
        downloadDir.mkdirs();
        File configDir = new File(workDirectory);
        workingDir = new File(configDir, "Working");
        workingDir.mkdirs();

        for(int idx = 0; idx < 5; ++idx){
            VFtpSssDownloadExecutorThread executorThread = new VFtpSssDownloadExecutorThread(mainQueueSss,
                    sssDownloadFileRepository,
                    downloadDir,
                    workingDir);
            vFtpSssDownloadExecutorThreadList.add(executorThread);
            Thread th = new Thread(executorThread);
            th.setPriority(Thread.NORM_PRIORITY - 1);
            th.start();
        }

        for(int idx = 0; idx < 5; ++idx){
            VFtpCompatDownloadExecutorThread executorThread = new VFtpCompatDownloadExecutorThread(mainQueueCompat,
                    compatDownloadFileRepository,
                    downloadDir,
                    workingDir);
            vFtpCompatDownloadExecutorThreadList.add(executorThread);
            Thread th = new Thread(executorThread);
            th.setPriority(Thread.NORM_PRIORITY - 1);
            th.start();
        }
    }

    public void stopAll(){
        System.out.println("VFtpDownloadService stopAll");
        for (VFtpSssDownloadExecutorThread executorThread : vFtpSssDownloadExecutorThreadList){
            executorThread.stop();
        }
        for (VFtpCompatDownloadExecutorThread executorThread : vFtpCompatDownloadExecutorThreadList){
            executorThread.stop();
        }
    }

    synchronized Date generateRequestTime(){
        final Date currentTime = new Date();
        if (lastRequestNumber >= currentTime.getTime()){
            currentTime.setTime(lastRequestNumber + 1);
        }
        lastRequestNumber = currentTime.getTime();
        return currentTime;
    }

    String generateSssRequestNoFromTime(Date requestTime, String machine){
        StringBuilder builder = new StringBuilder()
                .append("REQ_SSS_")
                .append(machine).append("_")
                .append(format.format(requestTime));
        return builder.toString();
    }

    void enqueueSssDownloadRequest(VFtpSssDownloadRequest request,
                                   String host,
                                   String user,
                                   String password) throws Exception {
        VFtpSssDownloadRequestEx requestEx = new VFtpSssDownloadRequestEx();
        requestEx.setRequest(request);
        requestEx.setHost(host);
        requestEx.setUser(user);
        requestEx.setPassword(password);
        sssDownloadFileRepository.addRequest(request);
        mainQueueSss.add(requestEx);
    }

    public VFtpSssDownloadRequest addSssDownloadRequest(VFtpSssDownloadRequest request,
                                                        String host,
                                                        String user,
                                                        String password) throws Exception {
        Date requestTime = generateRequestTime();
        String requestNo = generateSssRequestNoFromTime(requestTime, request.getMachine());

        request.setTimestamp(requestTime.getTime());
        request.setRequestNo(requestNo);
        if (request.isArchive()){
            request.setArchiveFileName(request.getDirectory() + ".zip");
            request.setArchiveFilePath(request.getRequestNo() + "/" + request.getArchiveFileName());
        }

        enqueueSssDownloadRequest(request, host, user, password);
        return request;
    }

    public VFtpSssDownloadRequest getSssDownloadRequest(String requestNo){
        if (requestNo == null){
            return null;
        }

        return sssDownloadFileRepository.getRequest(requestNo);
    }

    public boolean cancelSssDownloadRequest(String requestNo){
        for (VFtpSssDownloadRequestEx requestEx : mainQueueSss) {
            VFtpSssDownloadRequest request = requestEx.getRequest();
            if (request.getRequestNo().equals(requestNo)) {
                request.setStatus(FtpDownloadRequest.Status.CANCEL);
                sssDownloadFileRepository.removeRequest(request);
                return true;
            }
        }
        for (VFtpSssDownloadExecutorThread thread : vFtpSssDownloadExecutorThreadList){
            VFtpSssDownloadRequest request = thread.getCurrentRequest();
            if (request != null){
                if (request.getRequestNo().equals(requestNo)){
                    boolean rc = thread.stopRequestCommand(requestNo);
                    request.setStatus(FtpRequest.Status.CANCEL);
                    sssDownloadFileRepository.removeRequest(request);
                    return rc;
                }
            }
        }
        return false;
    }

    String generateCompatRequestNoFromTime(Date requestTime, String machine){
        StringBuilder builder = new StringBuilder()
                .append("REQ_COMPAT_")
                .append(machine).append("_")
                .append(format.format(requestTime));
        return builder.toString();
    }

    void enqueueCompatDownloadRequest(VFtpCompatDownloadRequest request,
                                      String host,
                                      String user,
                                      String password) throws Exception {
        VFtpCompatDownloadRequestEx requestEx = new VFtpCompatDownloadRequestEx();
        requestEx.setRequest(request);
        requestEx.setHost(host);
        requestEx.setUser(user);
        requestEx.setPassword(password);
        compatDownloadFileRepository.addRequest(request);
        mainQueueCompat.add(requestEx);
    }

    public VFtpCompatDownloadRequest addCompatDownloadRequest(VFtpCompatDownloadRequest request,
                                                              String host,
                                                              String user,
                                                              String password) throws Exception {
        Date requestTime = generateRequestTime();
        String requestNo = generateCompatRequestNoFromTime(requestTime, request.getMachine());
        request.setRequestNo(requestNo);
        if (request.isArchive()){
            request.setArchiveFileName(request.getFile().getName() + ".zip");
            request.setArchiveFilePath(request.getRequestNo() + "/" + request.getArchiveFileName());
        }

        enqueueCompatDownloadRequest(request, host, user, password);
        return request;
    }

    public VFtpCompatDownloadRequest getCompatDownloadRequest(String requestNo){
        if (requestNo == null){
            return null;
        }

        return compatDownloadFileRepository.getRequest(requestNo);
    }

    public boolean cancelCompatDownloadRequest(String requestNo){
        for (VFtpCompatDownloadRequestEx requestEx : mainQueueCompat) {
            VFtpCompatDownloadRequest request = requestEx.getRequest();
            if (request.getRequestNo().equals(requestNo)) {
                request.setStatus(FtpDownloadRequest.Status.CANCEL);
                compatDownloadFileRepository.removeRequest(request);
                return true;
            }
        }
        for (VFtpCompatDownloadExecutorThread thread : vFtpCompatDownloadExecutorThreadList){
            VFtpCompatDownloadRequest request = thread.getCurrentRequest();
            if (request != null){
                if (request.getRequestNo().equals(requestNo)){
                    boolean rc = thread.stopRequestCommand(requestNo);
                    request.setStatus(FtpRequest.Status.CANCEL);
                    compatDownloadFileRepository.removeRequest(request);
                    return rc;
                }
            }
        }
        return false;
    }

    @PreDestroy
    public void preDestroy(){
        this.stopAll();
    }
}
