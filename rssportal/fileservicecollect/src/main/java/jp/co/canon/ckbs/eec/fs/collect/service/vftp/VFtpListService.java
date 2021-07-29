package jp.co.canon.ckbs.eec.fs.collect.service.vftp;

import jp.co.canon.ckbs.eec.fs.collect.model.VFtpSssListRequest;
import jp.co.canon.ckbs.eec.fs.collect.service.FileServiceCollectException;
import jp.co.canon.ckbs.eec.fs.collect.service.configuration.FtpServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class VFtpListService {
    long lastRequestNumber = 0;
    DateFormat format = new SimpleDateFormat("yyMMddHHmmssSSS");

    StringToOtherTypeMap<VFtpSssListRequest> stringToOtherTypeMap = new StringToOtherTypeMap<>();
    StringToOtherTypeMap<SssListProcessThread> listRequestThreadMap = new StringToOtherTypeMap<>();
    SssListRequestQueue completedRequestQueue = new SssListRequestQueue();

    boolean stopPurgeThread = false;

    void purgeRequests(){
        long baseTime = System.currentTimeMillis() - 60*1000;
        VFtpSssListRequest request = completedRequestQueue.get();
        while (request != null && request.getCompletedTime() < baseTime) {
            stringToOtherTypeMap.remove(request.getRequestNo());
            completedRequestQueue.pop();
            request = completedRequestQueue.get();
        }
    }

    @PostConstruct
    void postConstruct(){
        Thread requestPurgeThread = new Thread(()->{
            while(!stopPurgeThread){
                try {
                    Thread.sleep(30*1000);
                    purgeRequests();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        requestPurgeThread.start();
    }

    public void requestCompleted(String requestNo){
        listRequestThreadMap.remove(requestNo);
        VFtpSssListRequest request = stringToOtherTypeMap.get(requestNo);
        completedRequestQueue.add(request);
    }

    void addRequest(VFtpSssListRequest request){
        request.setTimestamp(System.currentTimeMillis());
        stringToOtherTypeMap.put(request.getRequestNo(), request);
    }

    synchronized Date generateRequestTime(){
        final Date currentTime = new Date();
        if (lastRequestNumber >= currentTime.getTime()){
            currentTime.setTime(lastRequestNumber + 1);
        }
        lastRequestNumber = currentTime.getTime();
        return currentTime;
    }

    String generateRequestNoFromTime(Date requestTime, String machine, String directory){
        StringBuilder id = new StringBuilder()
                .append("REQ_SSSLIST_")
                .append(machine).append("_")
                .append(format.format(requestTime));
        return id.toString();
    }

    boolean isValidDirectory(String directory){
        return true;
    }

    public VFtpSssListRequest addListRequest(VFtpSssListRequest request, String host, String user, String password) throws FileServiceCollectException {
        FtpServerInfo ftpServerInfo = new FtpServerInfo();
        ftpServerInfo.setFtpmode("passive");
        ftpServerInfo.setHost(host);
        ftpServerInfo.setUser(user);
        ftpServerInfo.setPassword(password);
        if (isValidDirectory(request.getDirectory()) == false){
            log.error("Parameter directory is not valid ({})", request.getDirectory());
            throw new FileServiceCollectException(400, "Parameter(directory) is not valid");
        }

        Date requestTime = generateRequestTime();

        String requestNo = generateRequestNoFromTime(requestTime, request.getMachine(), request.getDirectory());
        request.setRequestNo(requestNo);
        addRequest(request);

        SssListProcessThread thread = new SssListProcessThread(request, ftpServerInfo, this);
        listRequestThreadMap.put(request.getRequestNo(), thread);
        thread.start();

        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return request;
    }

    public VFtpSssListRequest getListRequest(String requestNo){
        VFtpSssListRequest request = stringToOtherTypeMap.get(requestNo);
        if (request != null){
            return request;
        }
        return null;
    }

    public void cancelAndDeleteSssListRequest(String requestNo){
        SssListProcessThread listRequestThread = listRequestThreadMap.get(requestNo);
        if (listRequestThread != null){
            listRequestThread.stopExecute();
        }
    }
}
