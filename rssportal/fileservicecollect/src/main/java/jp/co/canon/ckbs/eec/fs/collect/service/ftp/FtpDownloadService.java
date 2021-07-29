package jp.co.canon.ckbs.eec.fs.collect.service.ftp;

import jp.co.canon.ckbs.eec.fs.collect.action.FtpCommandExecutorThread;
import jp.co.canon.ckbs.eec.fs.collect.action.FtpDownloadFileRepository;
import jp.co.canon.ckbs.eec.fs.collect.model.FtpDownloadRequest;
import jp.co.canon.ckbs.eec.fs.collect.model.FtpDownloadRequestEx;
import jp.co.canon.ckbs.eec.fs.collect.model.FtpRequest;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Slf4j
@Component
public class FtpDownloadService {
    @Autowired
    FtpDownloadFileRepository downloadFileRepository;

    @Value("${fileservice.collect.ftp.downloadDirectory}")
    String ftpDownloadDirectory;

    @Value("${fileservice.collect.workDirectory}")
    String workDirectory;

    BlockingDeque<FtpDownloadRequestEx> mainQueue = new LinkedBlockingDeque<>();
    long lastRequestNumber = 0;
    DateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    List<FtpCommandExecutorThread> commandExecutorThreadList = new ArrayList<>();

    @PostConstruct
    private void postConstruct(){
        File downDir = new File(ftpDownloadDirectory);
        if (!downDir.exists()){
            downDir.mkdirs();
        }

        File configDir = new File(workDirectory);
        File workDir = new File(configDir, "Working");
        for(int idx = 0; idx < 10; ++idx){
            FtpCommandExecutorThread commandExecutorThread = new FtpCommandExecutorThread(mainQueue,
                    downloadFileRepository,
                    workDir,
                    10);
            commandExecutorThreadList.add(commandExecutorThread);
            Thread th = new Thread(commandExecutorThread);
            th.setPriority(Thread.NORM_PRIORITY - 1);
            th.start();
        }
    }

    @PreDestroy
    public void stopAll(){
        for (FtpCommandExecutorThread commandExecutorThread : commandExecutorThreadList){
            commandExecutorThread.stop();
        }
        commandExecutorThreadList.clear();
    }

    Date generateRequestTime(){
        final Date currentTime = new Date();
        if (lastRequestNumber >= currentTime.getTime()){
            currentTime.setTime(lastRequestNumber + 1);
        }
        lastRequestNumber = currentTime.getTime();
        return currentTime;
    }

    String generateRequestNoFromTime(Date requestTime, String machine, String category){
        StringBuilder id = new StringBuilder()
                .append("REQ_")
                .append(machine).append("_")
                .append(category).append("_")
                .append(format.format(requestTime));
        return id.toString();
    }

    void enqueueDownloadRequest(FtpDownloadRequest request, String host, String user, String password) throws Exception{
        FtpDownloadRequestEx requestEx = new FtpDownloadRequestEx();
        requestEx.setRequest(request);
        requestEx.setHost(host);
        requestEx.setUser(user);
        requestEx.setPassword(password);
        downloadFileRepository.addRequest(request);
        mainQueue.add(requestEx);
    }

    public FtpDownloadRequest addDownloadRequest(FtpDownloadRequest request,
                                                 String host,
                                                 String user,
                                                 String password) throws Exception{
        final Date requestTime = generateRequestTime();

        String requestNo = generateRequestNoFromTime(requestTime, request.getMachine(), request.getCategory());
        request.setTimestamp(requestTime.getTime());
        request.setRequestNo(requestNo);
        if (request.isArchive()){
            request.setArchiveFileName(String.format("%s.zip", requestNo));
        }
        enqueueDownloadRequest(request, host, user, password);
        return request;
    }

    public FtpDownloadRequest[] getFtpDownloadRequest(String requestNo){
        if (requestNo == null){
            return new FtpDownloadRequest[0];
        }
        FtpDownloadRequest request = downloadFileRepository.getRequest(requestNo);
        if (request == null){
            return new FtpDownloadRequest[0];
        }
        return new FtpDownloadRequest[]{request};
    }

    public boolean cancelDownloadRequest(String requestNo){
        log.info("Cancel Download Request {}", requestNo);
        for (FtpDownloadRequestEx requestEx : mainQueue){
            FtpDownloadRequest request = requestEx.getRequest();
            if (request.getRequestNo().equals(requestNo)){
                request.setStatus(FtpDownloadRequest.Status.CANCEL);
                downloadFileRepository.removeRequest(request);
                return true;
            }
        }
        for (FtpCommandExecutorThread thread : commandExecutorThreadList){
            FtpDownloadRequest request = thread.getCurrentRequest();
            if (request != null){
                if (request.getRequestNo().equals(requestNo)) {
                    boolean rc = thread.stopRequestCommand(requestNo);
                    request.setStatus(FtpRequest.Status.CANCEL);
                    downloadFileRepository.removeRequest(request);
                    return rc;
                }
            }
        }
        return false;
    }
}
