package jp.co.canon.ckbs.eec.fs.manage.controller;

import jp.co.canon.ckbs.eec.fs.collect.controller.param.*;
import jp.co.canon.ckbs.eec.fs.manage.service.FileServiceManageException;
import jp.co.canon.ckbs.eec.fs.manage.service.VFtpFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class VFtpCommandController {
    @Autowired
    VFtpFileService fileService;

    @PostMapping(value="/vftp/sss/list/{machine}")
    ResponseEntity<VFtpSssListRequestResponse> createSssListRequest(@PathVariable String machine, @RequestBody CreateVFtpListRequestParam param){
        VFtpSssListRequestResponse res = null;
        try {
            res = fileService.createVFtpSssListRequest(machine, param.getDirectory());
        } catch (FileServiceManageException e) {
            log.error("createSssListRequest Error {}", e.getCode());
            res = new VFtpSssListRequestResponse();
            res.setErrorCode(e.getCode());
            res.setErrorMessage(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(e.getCode()).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping(value="/vftp/sss/list/{machine}/{requestNo}")
    ResponseEntity<VFtpSssListRequestResponse> getSssListRequest(@PathVariable String machine, @PathVariable String requestNo){
        VFtpSssListRequestResponse res = null;
        try {
            res = fileService.getVFtpSssListRequest(machine, requestNo);
        } catch (FileServiceManageException e) {
            log.error("getSssListRequest Error {}", e.getCode());
            res = new VFtpSssListRequestResponse();
            res.setErrorCode(e.getCode());
            res.setErrorMessage(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(e.getCode()).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @DeleteMapping(value="/vftp/sss/list/{machine}/{requestNo}")
    ResponseEntity<?> cancelAndDeleteSssListRequest(@PathVariable String machine, @PathVariable String requestNo){
        try {
            fileService.cancelAndDeleteVFtpSssListRequest(machine, requestNo);
        } catch (FileServiceManageException e) {
            log.error("cancelAndDeleteSssListRequest Error {}", e.getCode());
            e.printStackTrace();
        }
        return ResponseEntity.ok("cancelAndDeleteListRequest");
    }

    @PostMapping(value="/vftp/sss/download/{machine}")
    ResponseEntity<VFtpSssDownloadRequestResponse> createSssDownloadRequest(@PathVariable String machine, @RequestBody CreateVFtpSssDownloadRequestParam param){
        VFtpSssDownloadRequestResponse res = null;
        try {
            res = fileService.createVFtpSssDownloadRequest(machine, param.getDirectory(), param.getFileList(), param.isArchive());
        } catch (FileServiceManageException e) {
            log.error("createSssDownloadRequest Error {}", e.getCode());
            res = new VFtpSssDownloadRequestResponse();
            res.setErrorCode(e.getCode());
            res.setErrorMessage(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(e.getCode()).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping(value="/vftp/sss/download/{machine}/{requestNo}")
    ResponseEntity<VFtpSssDownloadRequestResponse> getSssDownloadRequest(@PathVariable String machine, @PathVariable String requestNo){
        VFtpSssDownloadRequestResponse res = null;
        try {
            res = fileService.getVFtpSssDownloadRequest(machine, requestNo);
        } catch (FileServiceManageException e) {
            log.error("getSssDownloadRequest Error {}", e.getCode());
            res = new VFtpSssDownloadRequestResponse();
            res.setErrorCode(e.getCode());
            res.setErrorMessage(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(e.getCode()).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @DeleteMapping(value="/vftp/sss/download/{machine}/{requestNo}")
    ResponseEntity<?> cancelAndDeleteSssDownloadRequest(@PathVariable String machine, @PathVariable String requestNo){
        try {
            fileService.cancelAndDeleteVFtpSssDownloadRequest(machine, requestNo);
        } catch (FileServiceManageException e) {
            log.error("cancelAndDeleteSssDownloadRequest Error {}", e.getCode());
            e.printStackTrace();
        }
        return ResponseEntity.ok("cancelAndDeleteSssDownloadRequest");
    }

    @PostMapping(value="/vftp/compat/download/{machine}")
    ResponseEntity<VFtpCompatDownloadRequestResponse> createCompatDownloadRequest(@PathVariable String machine, @RequestBody CreateVFtpCompatDownloadRequestParam param){
        VFtpCompatDownloadRequestResponse res = null;
        try {
            res = fileService.createVFtpCompatDownloadRequest(machine, param.getFilename(), param.isArchive());
        } catch (FileServiceManageException e) {
            log.error("createCompatDownloadRequest Error {}", e.getCode());
            res = new VFtpCompatDownloadRequestResponse();
            res.setErrorCode(e.getCode());
            res.setErrorMessage(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(e.getCode()).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping(value="/vftp/compat/download/{machine}/{requestNo}")
    ResponseEntity<VFtpCompatDownloadRequestResponse> getCompatDownloadRequest(@PathVariable String machine, @PathVariable String requestNo){
        VFtpCompatDownloadRequestResponse res = null;
        try {
            res = fileService.getVFtpCompatDownloadRequest(machine, requestNo);
        } catch (FileServiceManageException e) {
            log.error("getCompatDownloadRequest Error {}", e.getCode());
            res = new VFtpCompatDownloadRequestResponse();
            res.setErrorCode(e.getCode());
            res.setErrorMessage(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(e.getCode()).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @DeleteMapping(value="/vftp/compat/download/{machine}/{requestNo}")
    ResponseEntity<?> cancelAndDeleteCompatDownloadRequest(@PathVariable String machine, @PathVariable String requestNo){
        try {
            fileService.cancelAndDeleteVFtpCompatDownloadRequest(machine, requestNo);
        } catch (FileServiceManageException e) {
            log.error("cancelAndDeleteCompatDownloadRequest Error {}", e.getCode());
            e.printStackTrace();
        }
        return ResponseEntity.ok("cancelAndDeleteCompatDownloadRequest");
    }
}
