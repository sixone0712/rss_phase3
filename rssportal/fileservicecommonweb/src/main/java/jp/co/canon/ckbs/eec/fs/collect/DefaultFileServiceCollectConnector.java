package jp.co.canon.ckbs.eec.fs.collect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jp.co.canon.ckbs.eec.fs.collect.controller.param.*;
import jp.co.canon.ckbs.eec.fs.collect.service.LogFileList;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.ConfigurationService;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.category.CategoryInfo;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure.MpaInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class DefaultFileServiceCollectConnector implements FileServiceCollectConnector{
    ConfigurationService configurationService;
    RestTemplate restTemplate;
    String host;
    String prefix;
    ObjectMapper objectMapper = new ObjectMapper();

    static final int PROXY_ERROR_RETRY_MAX = 3;

    public DefaultFileServiceCollectConnector(String host, RestTemplate restTemplate, ConfigurationService configurationService){
        this.configurationService = configurationService;
        this.restTemplate = restTemplate;
        this.host = host;
        this.prefix = String.format("http://%s", this.host);
    }

    /*
    FTP INTERFACE
    */
    String generateFtpHostUrl(MpaInfo mpaInfo, CategoryInfo categoryInfo){
        return String.format("ftp://%s:%d%s", mpaInfo.getHost(), categoryInfo.getPort(), categoryInfo.getRootDir());
    }

    public LogFileList getFtpFileList(String machine,
                                      String category,
                                      String from,
                                      String to,
                                      String keyword,
                                      String path,
                                      boolean recursive){
        MpaInfo mpaInfo = configurationService.getMpaInfo(machine);
        CategoryInfo categoryInfo = configurationService.getCategory(category);

        FscListFilesRequestParam param = new FscListFilesRequestParam();
        param.setMachine(machine);
        param.setCategory(category);
        param.setFrom(from);
        param.setTo(to);
        param.setKeyword(keyword);
        param.setPath(path);
        param.setRecursive(recursive);

        param.setHost(generateFtpHostUrl(mpaInfo, categoryInfo));
        String pattern;
        if (categoryInfo.getPatternDir().endsWith("/")){
            pattern = categoryInfo.getPatternDir() + categoryInfo.getFileName();
        } else {
            pattern = String.format("%s/%s", categoryInfo.getPatternDir(), categoryInfo.getFileName());
        }
        param.setPattern(pattern);
        if (categoryInfo.getPort() == 21) {
            param.setUser(mpaInfo.getFtpUser());
            param.setPassword(mpaInfo.getFtpPassword());
        } else {
            param.setUser(mpaInfo.getVftpUser());
            param.setPassword(mpaInfo.getVftpPassword());
        }

        String url = this.prefix + "/fsc/ftp/files";
        String funcName = "getFtpFileList";
        try {
            int retryCount = 0;
            ResponseEntity<String> res = restTemplate.postForEntity(url, param, String.class);
            while(res.getStatusCodeValue() == 502 && retryCount < PROXY_ERROR_RETRY_MAX){
                Thread.sleep(500);
                log.info("{} proxy error {}th retry.", funcName, retryCount+1);
                res = restTemplate.postForEntity(url, param, String.class);
                retryCount++;
            }
            if (res.getStatusCodeValue() == 200){
                return objectMapper.readValue(res.getBody(), LogFileList.class);
            }
            if (res.getStatusCodeValue() == 502){
                log.error("{} proxy error retry failed", funcName);
                throw new RestClientException(String.format("proxy error retry failed"));
            }
            throw new RestClientException(String.format("%s return %d", funcName, res.getStatusCodeValue()));
        } catch (RestClientException | JsonProcessingException | InterruptedException e){
            log.error("{} RestClientException occurred ({})", funcName, e.getMessage());
            LogFileList logFileList = new LogFileList();
            logFileList.setErrorCode("500 RestClientException");
            logFileList.setErrorMessage(e.getMessage());
            return logFileList;
        }
    }

    FtpDownloadRequestResponse createFtpDownloadRequest(String machine, FscCreateFtpDownloadRequestParam param){
        String url = this.prefix + "/fsc/ftp/download";
        String funcName = "createFtpDownloadRequest";
        try {
            int retryCount = 0;
            ResponseEntity<String> res = restTemplate.postForEntity(url, param, String.class);
            while(res.getStatusCodeValue() == 502 && retryCount < PROXY_ERROR_RETRY_MAX){
                Thread.sleep(500);
                log.info("{} proxy error {}th retry.", funcName, retryCount+1);
                res = restTemplate.postForEntity(url, param, String.class);
                retryCount++;
            }
            if (res.getStatusCodeValue() == 200){
                return objectMapper.readValue(res.getBody(), FtpDownloadRequestResponse.class);
            }
            if (res.getStatusCodeValue() == 502){
                log.error("{} proxy error retry failed", funcName);
                throw new RestClientException(String.format("proxy error retry failed"));
            }
            throw new RestClientException(String.format("%s return %d", funcName, res.getStatusCodeValue()));
        } catch (RestClientException | JsonProcessingException | InterruptedException e){
            log.error("{} RestClientException occurred ({})", funcName, e.getMessage());
            FtpDownloadRequestResponse response = new FtpDownloadRequestResponse();
            response.setErrorCode("500 RestClientException");
            response.setErrorMessage(e.getMessage());
            return response;
        }
    }

    public FtpDownloadRequestResponse createFtpDownloadRequest(String machine, String category, boolean archive, String[] fileList){
        MpaInfo mpaInfo = configurationService.getMpaInfo(machine);
        CategoryInfo categoryInfo = configurationService.getCategory(category);

        FscCreateFtpDownloadRequestParam param = new FscCreateFtpDownloadRequestParam();
        param.setMachine(machine);
        param.setCategory(category);
        param.setArchive(archive);
        param.setFileList(fileList);

        param.setHost(generateFtpHostUrl(mpaInfo, categoryInfo));
        if (categoryInfo.getPort() == 21) {
            param.setUser(mpaInfo.getFtpUser());
            param.setPassword(mpaInfo.getFtpPassword());
        } else {
            param.setUser(mpaInfo.getVftpUser());
            param.setPassword(mpaInfo.getVftpPassword());
        }

        return createFtpDownloadRequest(machine, param);
    }

    public FtpDownloadRequestListResponse getFtpDownloadRequestList(String machine, String requestNo){
        String url = this.prefix + "/fsc/ftp/download/{requestNo}";
        String funcName = "getFtpDownloadRequestList";
        try {
            int retryCount = 0;
            ResponseEntity<String> res = restTemplate.getForEntity(url, String.class, requestNo);
            while(res.getStatusCodeValue() == 502 && retryCount < PROXY_ERROR_RETRY_MAX){
                Thread.sleep(500);
                log.info("{} proxy error {}th retry.", funcName, retryCount+1);
                res = restTemplate.getForEntity(url, String.class, requestNo);
                retryCount++;
            }
            if (res.getStatusCodeValue() == 200){
                return objectMapper.readValue(res.getBody(), FtpDownloadRequestListResponse.class);
            }
            if (res.getStatusCodeValue() == 502){
                log.error("{} proxy error retry failed", funcName);
                throw new RestClientException(String.format("proxy error retry failed"));
            }
            throw new RestClientException(String.format("%s return %d", funcName, res.getStatusCodeValue()));
        } catch (RestClientException | JsonProcessingException | InterruptedException e){
            log.error("{} RestClientException occurred ({})", funcName, e.getMessage());
            FtpDownloadRequestListResponse response = new FtpDownloadRequestListResponse();
            response.setErrorCode("500 RestClientException");
            response.setErrorMessage(e.getMessage());
            return response;
        }
    }

    public void cancelAndDeleteRequest(String machine, String requestNo){
        String url = this.prefix + "/fsc/ftp/download/{requestNo}";
        String funcName = "cancelAndDeleteRequest";
        try {
            int retryCount = 0;
            ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class, requestNo);
            while(res.getStatusCodeValue() == 502 && retryCount < PROXY_ERROR_RETRY_MAX){
                Thread.sleep(500);
                log.info("{} proxy error {}th retry.", funcName, retryCount+1);
                res = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class, requestNo);
                retryCount++;
            }
            if (res.getStatusCodeValue() == 200){
                return;
            }
            if (res.getStatusCodeValue() == 502){
                log.error("{} proxy error retry failed", funcName);
                throw new RestClientException(String.format("proxy error retry failed"));
            }
            throw new RestClientException(String.format("%s return %d", funcName, res.getStatusCodeValue()));
        } catch (RestClientException | InterruptedException e){
            log.error("{} RestClientException occurred ({})", funcName, e.getMessage());
            e.printStackTrace();
        }
    }

    /*
        VFTP INTERFACE
     */

    /* SSS LIST */
    public VFtpSssListRequestResponse createVFtpSssListRequest(String machine, String directory) {
        MpaInfo mpaInfo = configurationService.getMpaInfo(machine);
        String url = this.prefix + "/fsc/vftp/sss/list";
        String funcName = "createVFtpSssListRequest";

        FscCreateVFtpListRequestParam param = new FscCreateVFtpListRequestParam();
        param.setMachine(machine);
        param.setDirectory(directory);
        param.setHost(mpaInfo.getHost());
        param.setUser(mpaInfo.getVftpUser());
        param.setPassword(mpaInfo.getVftpPassword());
        try {
            int retryCount = 0;
            ResponseEntity<String> res = restTemplate.postForEntity(url, param, String.class, machine);
            while(res.getStatusCodeValue() == 502 && retryCount < PROXY_ERROR_RETRY_MAX){
                Thread.sleep(500);
                log.info("{} proxy error {}th retry.", funcName, retryCount+1);
                res = restTemplate.postForEntity(url, param, String.class, machine);
                retryCount++;
            }
            if (res.getStatusCodeValue() == 200){
                return objectMapper.readValue(res.getBody(), VFtpSssListRequestResponse.class);
            }
            if (res.getStatusCodeValue() == 502){
                log.error("{} proxy error retry failed", funcName);
                throw new RestClientException(String.format("proxy error retry failed"));
            }
            throw new RestClientException(String.format("%s return %d", funcName, res.getStatusCodeValue()));
        } catch (RestClientException | JsonProcessingException | InterruptedException e){
            log.error("{} RestClientException occurred ({})", funcName, e.getMessage());
            VFtpSssListRequestResponse response = new VFtpSssListRequestResponse();
            response.setErrorCode(500);
            response.setErrorMessage(e.getMessage());
            return response;
        }
    }

    public VFtpSssListRequestResponse getVFtpSssListRequest(String machine, String requestNo){
        String url = this.prefix + "/fsc/vftp/sss/list/{requestNo}";
        String funcName = "getVFtpSssListRequest";
        try {
            int retryCount = 0;
            ResponseEntity<String> res = restTemplate.getForEntity(url, String.class, requestNo);
            while(res.getStatusCodeValue() == 502 && retryCount < PROXY_ERROR_RETRY_MAX){
                Thread.sleep(500);
                log.info("{} proxy error {}th retry.", funcName, retryCount+1);
                res = restTemplate.getForEntity(url, String.class, requestNo);
                retryCount++;
            }
            if (res.getStatusCodeValue() == 200){
                return objectMapper.readValue(res.getBody(), VFtpSssListRequestResponse.class);
            }
            if (res.getStatusCodeValue() == 502){
                log.error("{} proxy error retry failed", funcName);
                throw new RestClientException(String.format("proxy error retry failed"));
            }
            throw new RestClientException(String.format("%s return %d", funcName, res.getStatusCodeValue()));
        } catch (RestClientException | JsonProcessingException | InterruptedException e){
            log.error("{} RestClientException occurred ({})", funcName, e.getMessage());
            VFtpSssListRequestResponse response = new VFtpSssListRequestResponse();
            response.setErrorCode(500);
            response.setErrorMessage(e.getMessage());
            return response;
        }
    }

    public void cancelAndDeleteVFtpSssListRequest(String machine, String requestNo){
        String url = this.prefix + "/fsc/vftp/sss/list/{requestNo}";
        String funcName = "cancelAndDeleteVFtpSssListRequest";
        try {
            int retryCount = 0;
            ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class, requestNo);
            while(res.getStatusCodeValue() == 502 && retryCount < PROXY_ERROR_RETRY_MAX){
                Thread.sleep(500);
                log.info("{} proxy error {}th retry.", funcName, retryCount+1);
                res = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class, requestNo);
                retryCount++;
            }
            if (res.getStatusCodeValue() == 200){
                return;
            }
            if (res.getStatusCodeValue() == 502){
                log.error("{} proxy error retry failed", funcName);
                throw new RestClientException(String.format("proxy error retry failed"));
            }
            throw new RestClientException(String.format("%s return %d", funcName, res.getStatusCodeValue()));
        } catch (RestClientException | InterruptedException e){
            log.error("{} RestClientException occurred ({})", funcName, e.getMessage());
            e.printStackTrace();
        }
    }

    /* SSS DOWNLOAD */
    public VFtpSssDownloadRequestResponse createVFtpSssDownloadRequest(String machine, String directory, String[] fileList, boolean archive){
        MpaInfo mpaInfo = configurationService.getMpaInfo(machine);
        String url = this.prefix + "/fsc/vftp/sss/download";
        String funcName = "createVFtpSssDownloadRequest";

        FscCreateVFtpSssDownloadRequestParam param = new FscCreateVFtpSssDownloadRequestParam();
        param.setDirectory(directory);
        param.setFileList(fileList);
        param.setArchive(archive);
        param.setMachine(machine);
        param.setHost(mpaInfo.getHost());
        param.setUser(mpaInfo.getVftpUser());
        param.setPassword(mpaInfo.getVftpPassword());
        try {
            int retryCount = 0;
            ResponseEntity<String> res = restTemplate.postForEntity(url, param, String.class);
            while(res.getStatusCodeValue() == 502 && retryCount < PROXY_ERROR_RETRY_MAX){
                Thread.sleep(500);
                log.info("{} proxy error {}th retry.", funcName, retryCount+1);
                res = restTemplate.postForEntity(url, param, String.class);
                retryCount++;
            }
            if (res.getStatusCodeValue() == 200){
                return objectMapper.readValue(res.getBody(), VFtpSssDownloadRequestResponse.class);
            }
            if (res.getStatusCodeValue() == 502){
                log.error("{} proxy error retry failed", funcName);
                throw new RestClientException(String.format("proxy error retry failed"));
            }
            throw new RestClientException(String.format("%s return %d", funcName, res.getStatusCodeValue()));
        } catch (RestClientException | JsonProcessingException | InterruptedException e){
            log.error("{} RestClientException occurred ({})", funcName, e.getMessage());
            VFtpSssDownloadRequestResponse response = new VFtpSssDownloadRequestResponse();
            response.setErrorCode(500);
            response.setErrorMessage(e.getMessage());
            return response;
        }
    }

    public VFtpSssDownloadRequestResponse getVFtpSssDownloadRequest(String machine, String requestNo){
        String url = this.prefix + "/fsc/vftp/sss/download/{requestNo}";
        String funcName = "getVFtpSssDownloadRequest";

        try {
            int retryCount = 0;
            ResponseEntity<String> res = restTemplate.getForEntity(url, String.class, requestNo);
            while(res.getStatusCodeValue() == 502 && retryCount < PROXY_ERROR_RETRY_MAX){
                Thread.sleep(500);
                log.info("{} proxy error {}th retry.", funcName, retryCount+1);
                res = restTemplate.getForEntity(url, String.class, requestNo);
                retryCount++;
            }
            if (res.getStatusCodeValue() == 200){
                return objectMapper.readValue(res.getBody(), VFtpSssDownloadRequestResponse.class);
            }
            if (res.getStatusCodeValue() == 502){
                log.error("{} proxy error retry failed", funcName);
                throw new RestClientException(String.format("proxy error retry failed"));
            }
            throw new RestClientException(String.format("%s return %d", funcName, res.getStatusCodeValue()));
        } catch (RestClientException | JsonProcessingException | InterruptedException e){
            log.error("{} RestClientException occurred ({})", funcName, e.getMessage());
            VFtpSssDownloadRequestResponse response = new VFtpSssDownloadRequestResponse();
            response.setErrorCode(500);
            response.setErrorMessage(e.getMessage());
            return response;
        }
    }

    public void cancelAndDeleteVFtpSssDownloadRequest(String machine, String requestNo){
        String url = this.prefix + "/fsc/vftp/sss/download/{requestNo}";
        String funcName = "cancelAndDeleteVFtpSssDownloadRequest";
        try {
            int retryCount = 0;
            ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class, requestNo);
            while(res.getStatusCodeValue() == 502 && retryCount < PROXY_ERROR_RETRY_MAX){
                Thread.sleep(500);
                log.info("{} proxy error {}th retry.", funcName, retryCount+1);
                res = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class, requestNo);
                retryCount++;
            }
            if (res.getStatusCodeValue() == 200){
                return;
            }
            if (res.getStatusCodeValue() == 502){
                log.error("{} proxy error retry failed", funcName);
                throw new RestClientException(String.format("proxy error retry failed"));
            }
            throw new RestClientException(String.format("%s return %d", funcName, res.getStatusCodeValue()));
        } catch (RestClientException | InterruptedException e){
            log.error("{} RestClientException occurred ({})", funcName, e.getMessage());
            e.printStackTrace();
        }
    }

    /* COMPAT DOWNLOAD */
    public VFtpCompatDownloadRequestResponse createVFtpCompatDownloadRequest(String machine, String filename, boolean archive){
        MpaInfo mpaInfo = configurationService.getMpaInfo(machine);
        String url = this.prefix + "/fsc/vftp/compat/download";
        String funcName = "createVFtpCompatDownloadRequest";

        FscCreateVFtpCompatDownloadRequestParam param = new FscCreateVFtpCompatDownloadRequestParam();
        param.setFilename(filename);
        param.setArchive(archive);
        param.setMachine(machine);
        param.setHost(mpaInfo.getHost());
        param.setUser(mpaInfo.getVftpUser());
        param.setPassword(mpaInfo.getVftpPassword());
        try {
            int retryCount = 0;
            ResponseEntity<String> res = restTemplate.postForEntity(url, param, String.class);
            while(res.getStatusCodeValue() == 502 && retryCount < PROXY_ERROR_RETRY_MAX){
                Thread.sleep(500);
                log.info("{} proxy error {}th retry.", funcName, retryCount+1);
                res = restTemplate.postForEntity(url, param, String.class);
                retryCount++;
            }
            if (res.getStatusCodeValue() == 200){
                return objectMapper.readValue(res.getBody(), VFtpCompatDownloadRequestResponse.class);
            }
            if (res.getStatusCodeValue() == 502){
                log.error("{} proxy error retry failed", funcName);
                throw new RestClientException(String.format("proxy error retry failed"));
            }
            throw new RestClientException(String.format("%s return %d", funcName, res.getStatusCodeValue()));
        } catch (RestClientException | JsonProcessingException | InterruptedException e){
            log.error("{} RestClientException occurred ({})", funcName, e.getMessage());
            VFtpCompatDownloadRequestResponse response = new VFtpCompatDownloadRequestResponse();
            response.setErrorCode(500);
            response.setErrorMessage(e.getMessage());
            return response;
        }
    }

    public VFtpCompatDownloadRequestResponse getVFtpCompatDownloadRequest(String machine, String requestNo){
        String url = this.prefix + "/fsc/vftp/compat/download/{requestNo}";
        String funcName = "getVFtpCompatDownloadRequest";
        try {
            int retryCount = 0;
            ResponseEntity<String> res = restTemplate.getForEntity(url, String.class, requestNo);
            while(res.getStatusCodeValue() == 502 && retryCount < PROXY_ERROR_RETRY_MAX){
                Thread.sleep(500);
                log.info("{} proxy error {}th retry.", funcName, retryCount+1);
                res = restTemplate.getForEntity(url, String.class, requestNo);
                retryCount++;
            }
            if (res.getStatusCodeValue() == 200){
                return objectMapper.readValue(res.getBody(), VFtpCompatDownloadRequestResponse.class);
            }
            if (res.getStatusCodeValue() == 502){
                log.error("{} proxy error retry failed", funcName);
                throw new RestClientException(String.format("proxy error retry failed"));
            }
            throw new RestClientException(String.format("%s return %d", funcName, res.getStatusCodeValue()));
        } catch (RestClientException | JsonProcessingException | InterruptedException e){
            log.error("{} RestClientException occurred ({})", funcName, e.getMessage());
            VFtpCompatDownloadRequestResponse response = new VFtpCompatDownloadRequestResponse();
            response.setErrorCode(500);
            response.setErrorMessage(e.getMessage());
            return response;
        }
    }

    public void cancelAndDeleteVFtpCompatDownloadRequest(String machine, String requestNo){
        String url = this.prefix + "/fsc/vftp/compat/download/{requestNo}";
        String funcName = "cancelAndDeleteVFtpCompatDownloadRequest";
        try {
            int retryCount = 0;
            ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class, requestNo);
            while(res.getStatusCodeValue() == 502 && retryCount < PROXY_ERROR_RETRY_MAX){
                Thread.sleep(500);
                log.info("{} proxy error {}th retry.", funcName, retryCount+1);
                res = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class, requestNo);
                retryCount++;
            }
            if (res.getStatusCodeValue() == 200){
                return;
            }
            if (res.getStatusCodeValue() == 502){
                log.error("{} proxy error retry failed", funcName);
                throw new RestClientException(String.format("proxy error retry failed"));
            }
            throw new RestClientException(String.format("%s return %d", funcName, res.getStatusCodeValue()));
        } catch (RestClientException | InterruptedException e){
            log.error("{} RestClientException occurred ({})", funcName, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean isServiceOn() {
        String url = this.prefix + "/fsc/common";
        try {
            ResponseEntity response = restTemplate.getForEntity(url, null);
            return response.getStatusCode()== HttpStatus.OK;
        } catch (Exception e) {
            log.error("{} service off {}", url, e.getMessage());
        }
        return false;
    }

    @Override
    public MachineStatusRequestResponse getMachineStatus(String machine) {
        String url = this.prefix + "/fsc/common/status";
        String funcName = "getMachineStatus";

        MpaInfo mpaInfo = configurationService.getMpaInfo(machine);
        if(mpaInfo==null) {
            log.error("{} invalid machine {}", funcName, machine);
            MachineStatusRequestResponse response = new MachineStatusRequestResponse();
            response.setMachine(machine);
            response.setErrorCode(400);
            response.setErrorMessage("invalid machine");
            return response;
        }

        FscMachineStatusRequestParam param = new FscMachineStatusRequestParam();
        param.setMachine(machine);
        param.setHost(mpaInfo.getHost());
        param.setFtpUser(mpaInfo.getFtpUser());
        param.setFtpPassword(mpaInfo.getFtpPassword());
        param.setVFtpUser(mpaInfo.getVftpUser());
        param.setVFtpPassword(mpaInfo.getVftpPassword());

        ResponseEntity<MachineStatusRequestResponse> response = restTemplate.postForEntity(
                url, param, MachineStatusRequestResponse.class);

        return response.getBody();
    }

    @Override
    public String downloadFile(String requestNo, String dest) {
        String url = this.prefix + "/fsc/download/{requestNo}";

        try {
            String downloadFile = restTemplate.execute(url, HttpMethod.GET, null, response -> {
                byte[] buffer = new byte[1024];
                int read;

                try (OutputStream outputStream = new FileOutputStream(dest)) {
                    InputStream inputStream = response.getBody();
                    while ((read = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, read);
                        outputStream.flush();
                    }
                    inputStream.close();
                }
                return dest;
            }, requestNo);
            return downloadFile;
        } catch (RestClientException e) {
            log.error("failed to download {} ({})", dest, e.getMessage());
            return null;
        }
    }
}
