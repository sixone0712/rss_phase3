package jp.co.canon.ckbs.eec.servicemanager.connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jp.co.canon.ckbs.eec.servicemanager.controller.ErrorInfo;
import jp.co.canon.ckbs.eec.servicemanager.controller.RestartResponse;
import jp.co.canon.ckbs.eec.servicemanager.service.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

public class DefaultServiceManagerConnector implements ServiceManagerConnector{
    RestTemplate restTemplate;
    String host;
    int port;
    String prefix;

    public DefaultServiceManagerConnector(String host, int port, RestTemplate restTemplate){
        this.host = host;
        this.port = port;
        this.restTemplate = restTemplate;
        this.prefix = String.format("http://%s:%d", this.host, this.port);
    }

    @Override
    public SystemInfo getSystemInfo(){
        ResponseEntity<SystemInfo> responseEntity = null;
        try {
            responseEntity = restTemplate.getForEntity(this.prefix + "/servicemanager/api/system", SystemInfo.class);
        } catch(RestClientException e){
            return null;
        }
        SystemInfo systemInfo = responseEntity.getBody();
        return systemInfo;
    }

    @Override
    public RestartResponse restartSystem(LoginInfo loginInfo) {
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.postForEntity(this.prefix + "/servicemanager/api/os/restart", loginInfo, String.class);
        } catch (RestClientException e){
            RestartResponse res = new RestartResponse();
            ErrorInfo err = new ErrorInfo();
            err.setCode(500203);
            err.setMessage("Communication Error with Server.");
            res.setError(err);
            return res;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = responseEntity.getBody();
        try {
            return objectMapper.readValue(jsonStr, RestartResponse.class);
        } catch (JsonProcessingException e) {
            RestartResponse res = new RestartResponse();
            ErrorInfo err = new ErrorInfo();
            err.setCode(500203);
            err.setMessage("Invalid Response from Server.");
            res.setError(err);
            return res;
        }
    }

    @Override
    public RestartResponse restartContainers(){
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.postForEntity(this.prefix + "/servicemanager/api/docker/restart", null, String.class);
        } catch (RestClientException e){
            RestartResponse res = new RestartResponse();
            ErrorInfo err = new ErrorInfo();
            err.setCode(500103);
            err.setMessage("Communication Error with OTS.");
            res.setError(err);
            return res;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = responseEntity.getBody();
        try {
            return objectMapper.readValue(jsonStr, RestartResponse.class);
        } catch (JsonProcessingException e) {
            RestartResponse res = new RestartResponse();
            ErrorInfo err = new ErrorInfo();
            err.setCode(500103);
            err.setMessage("Invalid Response from OTS.");
            res.setError(err);
            return res;
        }
    }

    @Override
    public LogFileList getFileList() throws Exception{
        LogFileList logFileList = null;
        ResponseEntity<LogFileList> responseEntity = null;
        responseEntity = restTemplate.getForEntity(this.prefix + "/servicemanager/api/files", LogFileList.class);
        logFileList = responseEntity.getBody();
        return logFileList;
    }

    @Override
    public CreateDownloadRequestResult createDownloadRequest(LogFileList logFileList) throws Exception{
        CreateDownloadRequestResult result = null;
        ResponseEntity<CreateDownloadRequestResult> responseEntity = null;
        responseEntity = restTemplate.postForEntity(this.prefix + "/servicemanager/api/files", logFileList, CreateDownloadRequestResult.class);
        result = responseEntity.getBody();
        return result;
    }

    @Override
    public DownloadRequestResult getDownloadRequest(String requestNo) throws Exception{
        DownloadRequestResult result = null;
        ResponseEntity<DownloadRequestResult> responseEntity = null;
        responseEntity = restTemplate.getForEntity(this.prefix + "/servicemanager/api/files/download/" + requestNo, DownloadRequestResult.class);
        result = responseEntity.getBody();
        return result;
    }

    @Override
    public void deleteDownloadRequest(String requestNo) throws Exception{
        restTemplate.delete(this.prefix + "/servicemanager/api/files/download/" + requestNo);
    }

    @Override
    public DownloadStreamInfo downloadFile(String requestNo) throws Exception{
        DownloadStreamInfo streamInfo = new DownloadStreamInfo();
        ResponseEntity<Resource> responseEntity = null;
        responseEntity = restTemplate.getForEntity(this.prefix + "/servicemanager/api/files/storage/" + requestNo, Resource.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            streamInfo.setInputStream(responseEntity.getBody().getInputStream());
            streamInfo.setContentLength(responseEntity.getHeaders().getContentLength());
            return streamInfo;
        }
        streamInfo.setErrorCode(500);
        return streamInfo;
    }
}

