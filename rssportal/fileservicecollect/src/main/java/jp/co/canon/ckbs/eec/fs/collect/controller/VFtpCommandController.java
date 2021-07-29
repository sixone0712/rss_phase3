package jp.co.canon.ckbs.eec.fs.collect.controller;

import jp.co.canon.ckbs.eec.fs.collect.controller.param.*;
import jp.co.canon.ckbs.eec.fs.collect.model.FtpRequest;
import jp.co.canon.ckbs.eec.fs.collect.model.VFtpCompatDownloadRequest;
import jp.co.canon.ckbs.eec.fs.collect.model.VFtpSssDownloadRequest;
import jp.co.canon.ckbs.eec.fs.collect.model.VFtpSssListRequest;
import jp.co.canon.ckbs.eec.fs.collect.service.FileServiceCollectException;
import jp.co.canon.ckbs.eec.fs.collect.service.vftp.VFtpFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class VFtpCommandController {
    @Autowired
    VFtpFileService fileService;

    @PostMapping(value="/vftp/sss/list")
    ResponseEntity<VFtpSssListRequestResponse> createSssListRequest(@RequestBody FscCreateVFtpListRequestParam param){
        VFtpSssListRequestResponse res = new VFtpSssListRequestResponse();
        try {
            log.info("createSssListRequest machine:{} host:{} dir:{}",
                    param.getMachine(),
                    param.getHost(),
                    param.getDirectory());

            VFtpSssListRequest request = fileService.addSssListRequest(param.getMachine(), param.getDirectory(), param.getHost(), param.getUser(), param.getPassword());
            res.setRequest(request);

            log.info("createSssListRequest machine:{} host:{} dir:{} success({})",
                    param.getMachine(),
                    param.getHost(),
                    param.getDirectory(),
                    request.getRequestNo());

            return ResponseEntity.ok(res);
        } catch(FileServiceCollectException e){
            log.error("createSssListRequest Error {}", e.getCode());
            res.setErrorCode(e.getCode());
            res.setErrorMessage(e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping(value="/vftp/sss/list/{requestNo}")
    ResponseEntity<VFtpSssListRequestResponse> getSssListRequest(@PathVariable String requestNo){
        VFtpSssListRequestResponse res = new VFtpSssListRequestResponse();
        try {
            VFtpSssListRequest request = fileService.getSssListRequest(requestNo);
            res.setRequest(request);
            return ResponseEntity.ok(res);
        } catch (FileServiceCollectException e) {
            log.error("getSssListRequest Error {}", e.getCode());
            res.setErrorCode(e.getCode());
            res.setErrorMessage(e.getMessage());
            return ResponseEntity.status(e.getCode()).body(res);
        }
    }

    @DeleteMapping(value="/vftp/sss/list/{requestNo}")
    ResponseEntity<?> cancelAndDeleteSssListRequest(@PathVariable String requestNo){
        log.info("cancelAndDeleteSssListRequest {}", requestNo);
        fileService.cancelAndDeleteSssListRequest(requestNo);
        return ResponseEntity.ok("cancelAndDeleteListRequest");
    }

    @PostMapping(value="/vftp/sss/download")
    ResponseEntity<VFtpSssDownloadRequestResponse> createSssDownloadRequest(@RequestBody FscCreateVFtpSssDownloadRequestParam param){
        VFtpSssDownloadRequestResponse res = new VFtpSssDownloadRequestResponse();
        try {
            log.info("createSssDownloadRequest machine:{} host:{} dir:{} requestFileCount:{}",
                    param.getMachine(),
                    param.getHost(),
                    param.getDirectory(),
                    param.getFileList().length
                    );

            VFtpSssDownloadRequest request = fileService.addSssDownloadRequest(param.getMachine(),
                    param.getDirectory(),
                    param.getFileList(),
                    param.isArchive(),
                    param.getHost(),
                    param.getUser(),
                    param.getPassword());
            res.setRequest(request);

            log.info("createSssDownloadRequest machine:{} host:{} dir:{} requestFileCount:{} success({})",
                    param.getMachine(),
                    param.getHost(),
                    param.getDirectory(),
                    param.getFileList().length,
                    request.getRequestNo()
            );

            return ResponseEntity.ok(res);
        } catch (FileServiceCollectException e) {
            log.error("createSssDownloadRequest Error {}", e.getCode());
            res.setErrorCode(e.getCode());
            res.setErrorMessage(e.getMessage());
            return ResponseEntity.status(e.getCode()).body(res);
        } catch (Exception e){
            log.error("createSssDownloadRequest Exception {}", e.getMessage());
            res.setErrorCode(500);
            res.setErrorMessage("Exception Occurred");
            return ResponseEntity.status(500).body(res);
        }
    }

    @GetMapping(value="/vftp/sss/download/{requestNo}")
    ResponseEntity<VFtpSssDownloadRequestResponse> getSssDownloadRequest(@PathVariable String requestNo){
        VFtpSssDownloadRequestResponse res = new VFtpSssDownloadRequestResponse();
        try {
            VFtpSssDownloadRequest request = null;
            request = fileService.getSssDownloadRequest(requestNo);
            res.setRequest(request);
            return ResponseEntity.ok(res);
        } catch (FileServiceCollectException e) {
            log.error("getSssDownloadRequest Error {}", e.getCode());
            res.setErrorCode(e.getCode());
            res.setErrorMessage(e.getMessage());
            return ResponseEntity.status(e.getCode()).body(res);
        }
    }

    @DeleteMapping(value="/vftp/sss/download/{requestNo}")
    ResponseEntity<?> cancelAndDeleteSssDownloadRequest(@PathVariable String requestNo){
        log.info("cancelAndDeleteSssDownloadRequest {}", requestNo);
        fileService.cancelAndDeleteSssDownloadRequest(requestNo);
        return ResponseEntity.ok("cancelAndDeleteSssDownloadRequest");
    }

    @PostMapping(value="/vftp/compat/download")
    ResponseEntity<VFtpCompatDownloadRequestResponse> createCompatDownloadRequest(@RequestBody FscCreateVFtpCompatDownloadRequestParam param){
        VFtpCompatDownloadRequestResponse res = new VFtpCompatDownloadRequestResponse();
        try {
            log.info("createCompatDownloadRequest machine:{} host:{} filename:{}",
                    param.getMachine(),
                    param.getHost(),
                    param.getFilename()
            );

            VFtpCompatDownloadRequest request = fileService.addCompatDownloadRequest(param.getMachine(),
                    param.getFilename(),
                    param.isArchive(),
                    param.getHost(),
                    param.getUser(),
                    param.getPassword());
            res.setRequest(request);

            log.info("createCompatDownloadRequest machine:{} host:{} filename:{} success({})",
                    param.getMachine(),
                    param.getHost(),
                    param.getFilename(),
                    request.getRequestNo()
            );
            return ResponseEntity.ok(res);
        } catch (FileServiceCollectException e) {
            log.error("createCompatDownloadRequest Error {}", e.getCode());
            res.setErrorCode(e.getCode());
            res.setErrorMessage(e.getMessage());
            return ResponseEntity.status(e.getCode()).body(res);
        } catch (Exception e){
            log.error("createSssDownloadRequest Exception {}", e.getMessage());
            res.setErrorCode(500);
            res.setErrorMessage("Exception Occurred");
            return ResponseEntity.status(500).body(res);
        }
    }

    @GetMapping(value="/vftp/compat/download/{requestNo}")
    ResponseEntity<VFtpCompatDownloadRequestResponse> getCompatDownloadRequest(@PathVariable String requestNo){
        VFtpCompatDownloadRequestResponse res = new VFtpCompatDownloadRequestResponse();
        try {
            VFtpCompatDownloadRequest request = fileService.getCompatDownloadRequest(requestNo);
            res.setRequest(request);
            return ResponseEntity.ok(res);
        } catch (FileServiceCollectException e) {
            log.error("getCompatDownloadRequest Error {}", e.getCode());
            res.setErrorCode(e.getCode());
            res.setErrorMessage(e.getMessage());
            return ResponseEntity.status(e.getCode()).body(res);
        }
    }

    @DeleteMapping(value="/vftp/compat/download/{requestNo}")
    ResponseEntity<?> cancelAndDeleteCompatDownloadRequest(@PathVariable String requestNo){
        log.info("cancelAndDeleteCompatDownloadRequest {}", requestNo);
        fileService.cancelAndDeleteCompatDownloadRequest(requestNo);
        return ResponseEntity.ok("cancelAndDeleteCompatDownloadRequest");
    }
}
