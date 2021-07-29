package jp.co.canon.ckbs.eec.servicemanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DownloadRequestRepository {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, DownloadRequest> downloadRequestMap = new HashMap<>();

    long lastRequestTime = 0;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    public String getRequestRootDirectory(){
        return "/CANON/LOG/downloads";
    }

    String getRequestDirectory(String requestNo){
        return String.format("%s/%s", getRequestRootDirectory(), requestNo);
    }

    String getRequestFileName(String requestNo){
        return String.format("%s.json", requestNo);
    }

    String getArchiveFilePath(String requestNo){
        return String.format("%s/%s.zip", getRequestDirectory(requestNo), requestNo);
    }

    public synchronized void saveRequestToFile(DownloadRequest request){
        String dirName = getRequestDirectory(request.getRequestNo());
        String fileName = getRequestFileName(request.getRequestNo());
        File dir = new File(dirName);
        dir.mkdirs();
        File requestFile = new File(dirName, fileName);
        try {
            objectMapper.writeValue(requestFile, request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void touchDirectory(String directory){
        File dir = new File(directory);
        if (dir.exists()){
            dir.setLastModified(System.currentTimeMillis());
        }
    }

    public synchronized DownloadRequest loadRequestFromFile(String requestNo){
        String dirName = getRequestDirectory(requestNo);
        String fileName = getRequestFileName(requestNo);
        File requestFile = new File(dirName, fileName);
        DownloadRequest request = null;
        touchDirectory(dirName);
        if (requestFile.exists() && requestFile.isFile()){
            try {
                request = objectMapper.readValue(requestFile, DownloadRequest.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return request;
    }

    synchronized String generateRequestNo(){
        long currentTime = System.currentTimeMillis();
        if (lastRequestTime == currentTime){
            currentTime = currentTime + 1;
        }
        lastRequestTime = currentTime;
        return String.format("REQSYS%s", simpleDateFormat.format(new Date(currentTime)));
    }

    public synchronized void addRequest(DownloadRequest request){
        String requestNo = generateRequestNo();
        request.setRequestNo(requestNo);
        downloadRequestMap.put(requestNo, request);
    }

    public synchronized DownloadRequest getRequest(String requestNo){
        DownloadRequest request = downloadRequestMap.get(requestNo);
        if (request != null){
            return request;
        }
        request = loadRequestFromFile(requestNo);
        return request;
    }

    public synchronized void downloadEnded(DownloadRequest request){
        saveRequestToFile(request);
        downloadRequestMap.remove(request.getRequestNo());
    }

    public synchronized void deleteRequest(String requestNo){
        downloadRequestMap.remove(requestNo);
        String dirName = getRequestDirectory(requestNo);
        File dir = new File(dirName);
        if (dir.exists()){
            try {
                FileUtils.deleteDirectory(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
