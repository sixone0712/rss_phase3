package jp.co.canon.ckbs.eec.fs.collect.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import jp.co.canon.ckbs.eec.fs.collect.model.FtpDownloadRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class FtpDownloadFileRepository {
    @Value("${fileservice.collect.ftp.downloadDirectory}")
    String downloadDirectory;

    File downDirFile;
    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, FtpDownloadRequest> requestMap = new HashMap<>();

    @PostConstruct
    void postConstruct(){
        downDirFile = new File(downloadDirectory);
        if (!downDirFile.exists()){
            downDirFile.mkdirs();
        }
    }

    String createRequestDirectory(String requestNo){
        return requestNo;
    }

    String createRequestDirectory(FtpDownloadRequest request){
        return createRequestDirectory(request.getRequestNo());
    }

    public void addRequest(FtpDownloadRequest request) throws IOException{
        String requestDirStr = createRequestDirectory(request);
        request.setDirectory(requestDirStr);
        if (request.isArchive()){
            request.setArchiveFilePath(String.format("%s/%s", request.getDirectory(), request.getArchiveFileName()));
        }
        File requestDownDirectory = new File(downloadDirectory, request.getDirectory());
        requestDownDirectory.mkdirs();
        synchronized (requestMap) {
            requestMap.put(request.getRequestNo(), request);
        }
    }

    synchronized void writeRequest(FtpDownloadRequest request, File file) throws IOException {
        try {
            objectMapper.writeValue(file, request);
        } catch (IOException e) {
            throw e;
        }
    }

    public void writeRequest(FtpDownloadRequest request) throws IOException {
        synchronized (request) {
            File requestDownDirectory = new File(downloadDirectory, request.getDirectory());
            requestDownDirectory.mkdirs();
            File requestFile = new File(requestDownDirectory, request.getRequestNo() + ".json");
            writeRequest(request, requestFile);
        }
    }

    public void endRequestExecution(FtpDownloadRequest request){
        synchronized (requestMap) {
            requestMap.remove(request.getRequestNo());
        }
    }

    public FtpDownloadRequest readRequest(File target) throws Exception{
        if (target == null){
            throw new Exception("file is not found");
        }
        if (!target.exists()){
            throw new Exception("file is not found");
        }
        FtpDownloadRequest request = objectMapper.readValue(target, FtpDownloadRequest.class);
        return request;
    }

    public FtpDownloadRequest getRequest(String requestNo){
        FtpDownloadRequest request = requestMap.get(requestNo);
        if (request == null) {
            String requestDirectory = createRequestDirectory(requestNo);
            File requestDownDirectory = new File(downloadDirectory, requestDirectory);
            if (requestDownDirectory.exists() && requestDownDirectory.isDirectory()) {
                requestDownDirectory.setLastModified(System.currentTimeMillis());
                File requestFile = new File(requestDownDirectory, requestNo + ".json");
                try {
                    request = readRequest(requestFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                log.error("FtpDownloadFileRepository#getRequest no file to read");
            }
        }
        return request;
    }

    static void deleteDirectory(File dir){
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeRequest(FtpDownloadRequest request){
        synchronized (request){
            File requestDownDirectory = new File(downloadDirectory, request.getDirectory());
            File requestFile = new File(requestDownDirectory, request.getRequestNo() + ".json");
            if (requestFile.exists()){
                deleteDirectory(requestDownDirectory);
            }
            synchronized (requestMap) {
                requestMap.remove(request.getRequestNo());
            }
        }
    }
}
