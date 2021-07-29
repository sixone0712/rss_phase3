package jp.co.canon.rss.logmanager.controller;

import jp.co.canon.rss.logmanager.system.FileUploadDownloadService;
import jp.co.canon.rss.logmanager.system.FileUploadResponse;
import jp.co.canon.rss.logmanager.config.ReqURLController;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@RestController
public class FileController {
    @Autowired
    private FileUploadDownloadService service;

    @PostMapping(ReqURLController.API_POST_UPLOADFILE)
    public FileUploadResponse uploadFile(@RequestParam("file") MultipartFile file) throws FileUploadException {
        String fileName = service.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new FileUploadResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
    }
}
