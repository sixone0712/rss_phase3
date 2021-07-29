package jp.co.canon.ckbs.eec.fs.collect.service.vftp;

import com.fasterxml.jackson.databind.ObjectMapper;
import jp.co.canon.ckbs.eec.fs.collect.model.VFtpSssDownloadRequest;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class SssDownloadFileRepository {
    @Value("${fileservice.collect.ftp.downloadDirectory}")
    String downloadDirectory;

    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, VFtpSssDownloadRequest> requestMap = new HashMap<>();

    String getRequestDirectory(String requestNo){
        return requestNo;
    }

    String getRequestDirectory(VFtpSssDownloadRequest request){
        return getRequestDirectory(request.getRequestNo());
    }

    public void addRequest(VFtpSssDownloadRequest request) throws IOException{
        writeRequest(request);
        synchronized (requestMap){
            requestMap.put(request.getRequestNo(), request);
        }
    }

    synchronized void writeRequest(VFtpSssDownloadRequest request, File file) throws IOException {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, request);
        } catch (IOException e){
            throw e;
        }
    }

    public void writeRequest(VFtpSssDownloadRequest request) throws IOException {
        synchronized (request){
            File requestDownDirectory = new File(downloadDirectory, getRequestDirectory(request));
            requestDownDirectory.mkdirs();
            File requestFile = new File(requestDownDirectory, request.getRequestNo()+".json");
            writeRequest(request, requestFile);
        }
    }

    public void endRequestExecution(VFtpSssDownloadRequest request){
        synchronized (requestMap) {
            requestMap.remove(request.getRequestNo());
        }
    }

    public VFtpSssDownloadRequest readRequest(File target) throws Exception {
        if (target == null){
            throw new Exception("request file is null");
        }
        if (!target.exists()){
            throw new Exception("request file is not found");
        }
        return objectMapper.readValue(target, VFtpSssDownloadRequest.class);
    }

    public VFtpSssDownloadRequest getRequest(String requestNo){
        VFtpSssDownloadRequest request = requestMap.get(requestNo);
        if (request == null){
            String requestDirectory = getRequestDirectory(requestNo);
            File requestDownDirectory = new File(downloadDirectory, requestDirectory);
            if (requestDownDirectory.exists() && requestDownDirectory.isDirectory()) {
                requestDownDirectory.setLastModified(System.currentTimeMillis());
                File requestFile = new File(requestDownDirectory, requestNo + ".json");
                try {
                    request = readRequest(requestFile);
                } catch (Exception e) {

                }
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

    public void removeRequest(VFtpSssDownloadRequest request){
        synchronized (request){
            File requestDownDirectory = new File(downloadDirectory, getRequestDirectory(request));
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
