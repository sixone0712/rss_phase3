package jp.co.canon.ckbs.eec.fs.collect.controller;

import jp.co.canon.ckbs.eec.fs.collect.controller.param.*;
import jp.co.canon.ckbs.eec.fs.collect.model.FtpDownloadRequest;
import jp.co.canon.ckbs.eec.fs.collect.model.FtpRequest;
import jp.co.canon.ckbs.eec.fs.collect.service.FileInfo;
import jp.co.canon.ckbs.eec.fs.collect.service.LogFileList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;

@SpringBootTest
public class FtpCommandControllerTest {
    @Autowired
    FtpCommandController controller;

    String ftpHost = "ftp://10.1.31.242/LOG/001";
    String ftpUser = "ckbs";
    String ftpPassword = "ckbs";

    @Test
    void test_ftp_list_001(){
        FscListFilesRequestParam param = new FscListFilesRequestParam();
        param.setHost(ftpHost);
        param.setUser(ftpUser);
        param.setPassword(ftpPassword);
        param.setMachine("MPA_1");
        param.setCategory("001");
        param.setPattern("*");
        param.setFrom("20201101000000");
        param.setTo("20201130235959");
        param.setPath("");
        param.setKeyword("");

        ResponseEntity<LogFileList> res = controller.getFtpFileList(param);
        LogFileList logFileList = res.getBody();
    }

    @Test
    void test_ftp_download_001(){
        FscListFilesRequestParam param = new FscListFilesRequestParam();
        param.setHost(ftpHost);
        param.setUser(ftpUser);
        param.setPassword(ftpPassword);
        param.setMachine("MPA_1");
        param.setCategory("001");
        param.setPattern("*");
        param.setFrom("20201101000000");
        param.setTo("20201130235959");
        param.setPath("");
        param.setKeyword("");

        ResponseEntity<LogFileList> res = controller.getFtpFileList(param);
        LogFileList logFileList = res.getBody();

        ArrayList<String> fileNamesArrayList = new ArrayList<>();
        FileInfo[] fileInfos = logFileList.getList();
        for(FileInfo fi : fileInfos){
            if (fi.getType().equals("F")){
                fileNamesArrayList.add(fi.getFilename());
            }
        }

        FscCreateFtpDownloadRequestParam param2 = new FscCreateFtpDownloadRequestParam();
        param2.setHost(ftpHost);
        param2.setUser(ftpUser);
        param2.setPassword(ftpPassword);
        param2.setMachine("MPA_1");
        param2.setCategory("001");
        param2.setArchive(true);
        param2.setFileList(fileNamesArrayList.toArray(new String[0]));

        ResponseEntity<FtpDownloadRequestResponse> res2 = controller.createFtpDownloadRequest(param2);
        FtpDownloadRequestResponse reqres2 = res2.getBody();
        String requestNo = reqres2.getRequestNo();

        ResponseEntity<FtpDownloadRequestListResponse> res3 =
                controller.getFtpDownloadRequestList(requestNo, "");
        controller.cancelAndDeleteFtpDownloadRequest(requestNo);
    }

    @Test
    void test_ftp_download_002(){
        FscListFilesRequestParam param = new FscListFilesRequestParam();
        param.setHost(ftpHost);
        param.setUser(ftpUser);
        param.setPassword(ftpPassword);
        param.setMachine("MPA_1");
        param.setCategory("001");
        param.setPattern("*");
        param.setFrom("20201101000000");
        param.setTo("20201130235959");
        param.setPath("");
        param.setKeyword("");

        ResponseEntity<LogFileList> res = controller.getFtpFileList(param);
        LogFileList logFileList = res.getBody();

        ArrayList<String> fileNamesArrayList = new ArrayList<>();
        FileInfo[] fileInfos = logFileList.getList();
        for(FileInfo fi : fileInfos){
            if (fi.getType().equals("F")){
                fileNamesArrayList.add(fi.getFilename());
            }
        }

        FscCreateFtpDownloadRequestParam param2 = new FscCreateFtpDownloadRequestParam();
        param2.setHost(ftpHost);
        param2.setUser(ftpUser);
        param2.setPassword(ftpPassword);
        param2.setMachine("MPA_1");
        param2.setCategory("001");
        param2.setArchive(true);
        param2.setFileList(fileNamesArrayList.toArray(new String[0]));

        ResponseEntity<FtpDownloadRequestResponse> res2 = controller.createFtpDownloadRequest(param2);
        FtpDownloadRequestResponse reqres2 = res2.getBody();
        String requestNo = reqres2.getRequestNo();

        ResponseEntity<FtpDownloadRequestListResponse> res3 =
                controller.getFtpDownloadRequestList(requestNo, "");
        FtpDownloadRequestListResponse requestListResponse = res3.getBody();
        FtpDownloadRequest request = requestListResponse.getRequestList()[0];

        while(request.getStatus() != FtpRequest.Status.EXECUTED){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            res3 = controller.getFtpDownloadRequestList(requestNo, "");
            requestListResponse = res3.getBody();
            request = requestListResponse.getRequestList()[0];
        }
    }
}
