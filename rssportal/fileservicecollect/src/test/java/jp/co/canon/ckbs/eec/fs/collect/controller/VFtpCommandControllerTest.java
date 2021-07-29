package jp.co.canon.ckbs.eec.fs.collect.controller;

import jp.co.canon.ckbs.eec.fs.collect.controller.param.*;
import jp.co.canon.ckbs.eec.fs.collect.model.FtpRequest;
import jp.co.canon.ckbs.eec.fs.collect.model.VFtpCompatDownloadRequest;
import jp.co.canon.ckbs.eec.fs.collect.model.VFtpSssDownloadRequest;
import jp.co.canon.ckbs.eec.fs.collect.model.VFtpSssListRequest;
import jp.co.canon.ckbs.eec.fs.collect.service.VFtpFileInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VFtpCommandControllerTest {
    @Autowired
    VFtpCommandController controller;
    String ftpHost = "10.1.31.242";
    String ftpUser = "ckbs";
    String ftpPassword = "ckbs";

    @Test
    void test_sss_list_001(){
        FscCreateVFtpListRequestParam param = new FscCreateVFtpListRequestParam();
        param.setHost(ftpHost);
        param.setMachine("MPA_1");
        param.setUser("ckbs");
        param.setPassword("ckbs");
        param.setDirectory("IP_AS_RAW-20201130_000000-20201130_235959");

        ResponseEntity<VFtpSssListRequestResponse> res = controller.createSssListRequest(param);

        VFtpSssListRequestResponse reqres = res.getBody();
        VFtpSssListRequest request = reqres.getRequest();
        String requestNo = request.getRequestNo();

        res = controller.getSssListRequest(requestNo);
        reqres = res.getBody();
        request = reqres.getRequest();

        while(request.getStatus() != FtpRequest.Status.EXECUTED){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            res = controller.getSssListRequest(requestNo);
            reqres = res.getBody();
            request = reqres.getRequest();
        }
    }

    @Test
    void test_sss_list_002(){
        FscCreateVFtpListRequestParam param = new FscCreateVFtpListRequestParam();
        param.setHost(ftpHost);
        param.setMachine("MPA_1");
        param.setUser("ckbs");
        param.setPassword("ckbs");
        param.setDirectory("IP_AS_RAW-20201130_000000-20201130_235959");

        ResponseEntity<VFtpSssListRequestResponse> res = controller.createSssListRequest(param);

        VFtpSssListRequestResponse reqres = res.getBody();
        VFtpSssListRequest request = reqres.getRequest();
        String requestNo = request.getRequestNo();
        controller.cancelAndDeleteSssListRequest(requestNo);
    }

    @Test
    void test_sss_download_001(){
        FscCreateVFtpListRequestParam param = new FscCreateVFtpListRequestParam();
        param.setHost(ftpHost);
        param.setMachine("MPA_1");
        param.setUser("ckbs");
        param.setPassword("ckbs");
        param.setDirectory("IP_AS_RAW-20201130_000000-20201130_235959");

        ResponseEntity<VFtpSssListRequestResponse> res = controller.createSssListRequest(param);

        VFtpSssListRequestResponse reqres = res.getBody();
        VFtpSssListRequest request = reqres.getRequest();
        String requestNo = request.getRequestNo();

        while(request.getStatus() != FtpRequest.Status.EXECUTED){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            res = controller.getSssListRequest(requestNo);
            reqres = res.getBody();
            request = reqres.getRequest();
        }

        ArrayList<String> fileArrayList = new ArrayList<>();
        for(VFtpFileInfo fileInfo : request.getFileList()){
            if (fileInfo.getFileType().equals("F")){
                fileArrayList.add(fileInfo.getFileName());
            }
        }

        FscCreateVFtpSssDownloadRequestParam param2 = new FscCreateVFtpSssDownloadRequestParam();
        param2.setHost(ftpHost);
        param2.setUser(ftpUser);
        param2.setPassword(ftpPassword);
        param2.setArchive(true);
        param2.setDirectory("IP_AS_RAW-20201130_000000-20201130_235959");
        param2.setMachine("MPA_2");
        param2.setFileList(fileArrayList.toArray(new String[0]));

        ResponseEntity<VFtpSssDownloadRequestResponse> res2 =
                controller.createSssDownloadRequest(param2);
        VFtpSssDownloadRequestResponse reqres2 = res2.getBody();
        VFtpSssDownloadRequest request2 = reqres2.getRequest();
        requestNo = request2.getRequestNo();

        while(request2.getStatus() != FtpRequest.Status.EXECUTED){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            res2 = controller.getSssDownloadRequest(requestNo);
            reqres2 = res2.getBody();
            request2 = reqres2.getRequest();
        }
    }

    @Test
    void test_sss_download_002(){
        FscCreateVFtpListRequestParam param = new FscCreateVFtpListRequestParam();
        param.setHost(ftpHost);
        param.setMachine("MPA_1");
        param.setUser("ckbs");
        param.setPassword("ckbs");
        param.setDirectory("IP_AS_RAW-20201130_000000-20201130_235959");

        ResponseEntity<VFtpSssListRequestResponse> res = controller.createSssListRequest(param);

        VFtpSssListRequestResponse reqres = res.getBody();
        VFtpSssListRequest request = reqres.getRequest();
        String requestNo = request.getRequestNo();

        while(request.getStatus() != FtpRequest.Status.EXECUTED){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            res = controller.getSssListRequest(requestNo);
            reqres = res.getBody();
            request = reqres.getRequest();
        }

        ArrayList<String> fileArrayList = new ArrayList<>();
        for(VFtpFileInfo fileInfo : request.getFileList()){
            if (fileInfo.getFileType().equals("F")){
                fileArrayList.add(fileInfo.getFileName());
            }
        }

        FscCreateVFtpSssDownloadRequestParam param2 = new FscCreateVFtpSssDownloadRequestParam();
        param2.setHost(ftpHost);
        param2.setUser(ftpUser);
        param2.setPassword(ftpPassword);
        param2.setArchive(true);
        param2.setDirectory("IP_AS_RAW-20201130_000000-20201130_235959");
        param2.setMachine("MPA_2");
        param2.setFileList(fileArrayList.toArray(new String[0]));

        ResponseEntity<VFtpSssDownloadRequestResponse> res2 =
                controller.createSssDownloadRequest(param2);
        VFtpSssDownloadRequestResponse reqres2 = res2.getBody();
        VFtpSssDownloadRequest request2 = reqres2.getRequest();
        requestNo = request2.getRequestNo();
        controller.cancelAndDeleteSssDownloadRequest(requestNo);
    }

    @Test
    void test_compat_download_001(){
        FscCreateVFtpCompatDownloadRequestParam param = new FscCreateVFtpCompatDownloadRequestParam();
        param.setArchive(true);
        param.setFilename("20201130_000000-20201130_235959.log");
        param.setHost(ftpHost);
        param.setMachine("MPA_1");
        param.setUser(ftpUser);
        param.setPassword(ftpPassword);

        ResponseEntity<VFtpCompatDownloadRequestResponse> res =
                controller.createCompatDownloadRequest(param);
        VFtpCompatDownloadRequestResponse reqres = res.getBody();
        VFtpCompatDownloadRequest request = reqres.getRequest();
        String requestNo = request.getRequestNo();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        res = controller.getCompatDownloadRequest(requestNo);
        reqres = res.getBody();
        request = reqres.getRequest();

        Assertions.assertEquals(requestNo, request.getRequestNo());
    }

    @Test
    void test_compat_download_002(){
        FscCreateVFtpCompatDownloadRequestParam param = new FscCreateVFtpCompatDownloadRequestParam();
        param.setArchive(true);
        param.setFilename("20201130_000000-20201130_235959.log");
        param.setHost(ftpHost);
        param.setMachine("MPA_1");
        param.setUser(ftpUser);
        param.setPassword(ftpPassword);

        ResponseEntity<VFtpCompatDownloadRequestResponse> res =
                controller.createCompatDownloadRequest(param);
        VFtpCompatDownloadRequestResponse reqres = res.getBody();
        VFtpCompatDownloadRequest request = reqres.getRequest();
        String requestNo = request.getRequestNo();

        controller.cancelAndDeleteCompatDownloadRequest(requestNo);
    }
}

