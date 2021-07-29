package jp.co.canon.ckbs.eec.servicemanager.controller;

import jp.co.canon.ckbs.eec.servicemanager.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@RestController
public class FileDownloadController {
    @Autowired
    FileDownloadService downloadService;

    @GetMapping(value="/api/files")
    ResponseEntity<?> getFileList(@RequestParam(required = false) String device){
        try {
            LogFileList logFileList = downloadService.getFileList(device);
            return ResponseEntity.ok(logFileList);
        } catch (Exception e){
            return ResponseEntity.status(500).body("");
        }
    }

    @PostMapping(value="/api/files")
    ResponseEntity<?> createDownloadRequest(@RequestParam(required = false) String device,
                                                                      @RequestBody LogFileList logFileList){
        try {
            CreateDownloadRequestResult result = downloadService.createDownloadRequest(device, logFileList);
            return ResponseEntity.ok(result);
        } catch (Exception e){
            return ResponseEntity.status(500).body("");
        }
    }

    @GetMapping(value="/api/files/download/{requestNo}")
    ResponseEntity<?> getDownloadRequest(@PathVariable String requestNo){
        try {
            DownloadRequestResult result = downloadService.getDownloadRequest(requestNo);
            return ResponseEntity.ok(result);
        } catch(Exception e){
            return ResponseEntity.status(500).body("");
        }
    }

    @DeleteMapping(value="/api/files/download/{requestNo}")
    ResponseEntity<?> deleteDownloadRequest(@PathVariable String requestNo){
        try {
            downloadService.deleteDownloadRequest(requestNo);
            return ResponseEntity.ok("");
        } catch(Exception e){
            return ResponseEntity.status(500).body("");
        }
    }

    @GetMapping(value="/api/files/storage/{requestNo}")
    ResponseEntity<?> downloadFile(@PathVariable String requestNo){
        try {
            DownloadStreamInfo streamInfo = downloadService.getDownloadStreamInfo(requestNo);
            if (streamInfo.getInputStream() != null) {
                InputStreamResource isr = new InputStreamResource(streamInfo.getInputStream());
                HttpHeaders headers = new HttpHeaders();
                headers.setContentLength(streamInfo.getContentLength());
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                ContentDisposition.Builder builder = ContentDisposition.builder("attachment");
                builder.filename(String.format("%s.zip", requestNo));
                headers.setContentDisposition(builder.build());
                return ResponseEntity.ok().headers(headers).body(isr);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("file not found");
        } catch(Exception e){
            return ResponseEntity.status(500).body("");
        }
    }
}
