package jp.co.canon.ckbs.eec.fs.collect.controller;

import jp.co.canon.ckbs.eec.fs.collect.model.FtpDownloadRequest;
import jp.co.canon.ckbs.eec.fs.collect.model.FtpRequest;
import jp.co.canon.ckbs.eec.fs.collect.model.VFtpCompatDownloadRequest;
import jp.co.canon.ckbs.eec.fs.collect.model.VFtpSssDownloadRequest;
import jp.co.canon.ckbs.eec.fs.collect.service.FileServiceCollectException;
import jp.co.canon.ckbs.eec.fs.collect.service.ftp.FtpFileService;
import jp.co.canon.ckbs.eec.fs.collect.service.vftp.VFtpFileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

@Slf4j
@RestController
public class FileDownloadController {

    @Autowired
    FtpFileService fileService;

    @Autowired
    VFtpFileService vFileService;

    @Value("${fileservice.collect.ftp.downloadDirectory}")
    String ftpDownloadDirectory;

    @AllArgsConstructor
    class DownloadFileInfo {
        String fileName;
        String filePath;
        long size;
    }

    private DownloadFileInfo getDownloadFileInfo(String requestNo) {
        Predicate<FtpRequest> isExecuted = req->{
            if(req.getStatus()==FtpRequest.Status.EXECUTED) {
                return true;
            }
            log.warn("download: {} is executing", req.getRequestNo());
            return false;
        };

        try {
            if(requestNo.startsWith("REQ_COMPAT_")) {
                VFtpCompatDownloadRequest request = vFileService.getCompatDownloadRequest(requestNo);
                if(isExecuted.test(request)) {
                    File file = Paths.get(ftpDownloadDirectory, request.getArchiveFilePath()).toFile();
                    if(file.exists()) {
                        return new DownloadFileInfo(request.getArchiveFileName(), request.getArchiveFilePath(), file.length());
                    }
                    log.error("download error: cannot find archive file {}", request.getArchiveFilePath());
                }
            } else if(requestNo.startsWith("REQ_SSS_")) {
                VFtpSssDownloadRequest request = vFileService.getSssDownloadRequest(requestNo);
                if(isExecuted.test(request)) {
                    File file = Paths.get(ftpDownloadDirectory, request.getArchiveFilePath()).toFile();
                    if(file.exists()) {
                        return new DownloadFileInfo(request.getArchiveFileName(), request.getArchiveFilePath(), file.length());
                    }
                    log.error("download error: cannot find archive file {}", request.getArchiveFilePath());
                }
            } else if(requestNo.startsWith("REQ_")) {
                FtpDownloadRequest[] requestList = fileService.getFtpDownloadRequest(requestNo);
                FtpDownloadRequest request;
                request = Arrays.stream(requestList).filter(item -> item.getRequestNo().equals(requestNo))
                        .findFirst().get();
                if(isExecuted.test(request)) {
                    return new DownloadFileInfo(request.getArchiveFileName(), request.getArchiveFilePath(), request.getArchiveFileSize());
                }
            } else {
                log.error("download error: invalid request {}", requestNo);
            }
        } catch (FileServiceCollectException e) {
            log.error("download error: {}", e.getMessage());
        } catch(NoSuchElementException e) {
            log.error("download error: invalid request {} {}", requestNo, e.getMessage());
        }
        return null;
    }

    @GetMapping("/download/{requestNo}")
    public ResponseEntity getLogFile(@PathVariable String requestNo) {
        log.info("getLogFile {}", requestNo);

        DownloadFileInfo downloadFileInfo = getDownloadFileInfo(requestNo);
        if(downloadFileInfo==null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        try {
            File archive = Paths.get(ftpDownloadDirectory, downloadFileInfo.filePath).toFile();
            InputStreamResource inputStreamResource = new InputStreamResource(new FileInputStream(archive));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentLength(downloadFileInfo.size);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            ContentDisposition.Builder builder = ContentDisposition.builder("attachment");
            builder.filename(downloadFileInfo.fileName);
            headers.setContentDisposition(builder.build());

            return new ResponseEntity(inputStreamResource, headers, HttpStatus.OK);
        } catch (FileNotFoundException e) {
            log.error("cannot read file {}", downloadFileInfo.filePath);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
