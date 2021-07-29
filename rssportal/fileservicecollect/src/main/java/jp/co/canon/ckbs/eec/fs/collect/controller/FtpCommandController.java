package jp.co.canon.ckbs.eec.fs.collect.controller;

import jp.co.canon.ckbs.eec.fs.collect.controller.param.*;
import jp.co.canon.ckbs.eec.fs.collect.model.FtpDownloadRequest;
import jp.co.canon.ckbs.eec.fs.collect.service.ftp.FtpFileService;
import jp.co.canon.ckbs.eec.fs.collect.service.LogFileList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class FtpCommandController {
    @Autowired
    FtpFileService service;

    @PostMapping(value="/ftp/files")
    ResponseEntity<LogFileList> getFtpFileList(@RequestBody FscListFilesRequestParam param){
        log.info("getFtpFileList> cat:{} host:{} pattern:{} from:{} to:{} dir:{} keyword:{} recursive:{}",
                param.getCategory(),
                param.getHost(),
                param.getPattern(),
                param.getFrom(),
                param.getTo(),
                param.getPath(),
                param.getKeyword(),
                param.isRecursive()
        );

        LogFileList logFileList = service.getLogFileList(param.getHost(),
                param.getPattern(),
                param.getFrom(),
                param.getTo(),
                param.getUser(),
                param.getPassword(),
                param.getPath(),
                param.getKeyword(),
                param.isRecursive());
        
        if (logFileList.getErrorCode() != null){
            String errCode = logFileList.getErrorCode();
            if (errCode.startsWith("400")){
                log.error("getFtpFileList 400 Error host={} path={}", param.getHost(), param.getPath());
                return ResponseEntity.badRequest().body(logFileList);
            }
            if (errCode.startsWith("500")){
                log.error("getFtpFileList 500 Error host={} path={}", param.getHost(), param.getPath());
                return ResponseEntity.status(500).body(logFileList);
            }
        }
        log.info("getFtpFileList< cat:{} host:{} pattern:{} from:{} to:{} dir:{} keyword:{} result count:{}",
                param.getCategory(),
                param.getHost(),
                param.getPattern(),
                param.getFrom(),
                param.getTo(),
                param.getPath(),
                param.getKeyword(),
                logFileList.getList().length
                );
        return ResponseEntity.ok(logFileList);
    }

    @PostMapping(value="/ftp/download")
    ResponseEntity<FtpDownloadRequestResponse> createFtpDownloadRequest(
            @RequestBody FscCreateFtpDownloadRequestParam param
    ){
        try {
            FtpDownloadRequestResponse res;
            FtpDownloadRequest request;
            log.info("createFtpDownloadRequest ca:{} host:{} reqFileCount:{}",
                    param.getCategory(),
                    param.getHost(),
                    param.getFileList().length
                    );
            request = service.addDownloadRequest(param.getMachine(),
                    param.getCategory(),
                    param.getFileList(),
                    param.isArchive(),
                    param.getHost(),
                    param.getUser(),
                    param.getPassword());
            res = FtpDownloadRequestResponse.fromRequest(request);
            log.info("createFtpDownloadRequest ca:{} host:{} reqFileCount:{} success({})",
                    param.getCategory(),
                    param.getHost(),
                    param.getFileList().length,
                    request.getRequestNo()
            );
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("createFtpDownloadRequest 400 Error");
            FtpDownloadRequestResponse res = new FtpDownloadRequestResponse();
            res.setErrorCode("400 Bad Request");
            res.setErrorMessage(e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping(value="/ftp/download/{requestNo}")
    ResponseEntity<FtpDownloadRequestListResponse> getFtpDownloadRequestList(
            @PathVariable String requestNo,
            @RequestParam(name="status", required = false) String status
    ){
        FtpDownloadRequest[] requestList = service.getFtpDownloadRequest(requestNo);
        FtpDownloadRequestListResponse res = new FtpDownloadRequestListResponse();
        res.setRequestList(requestList);

        return ResponseEntity.ok(res);
    }

    @DeleteMapping(value="/ftp/download/{requestNo}")
    ResponseEntity<?> cancelAndDeleteFtpDownloadRequest(
            @PathVariable String requestNo
    ){
        log.info("cancelAndDelteFtpDownloadRequest {}", requestNo);
        service.cancelDownloadRequest(requestNo);
        return ResponseEntity.ok().body("");
    }
}
