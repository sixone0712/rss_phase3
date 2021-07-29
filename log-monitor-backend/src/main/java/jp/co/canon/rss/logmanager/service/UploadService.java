package jp.co.canon.rss.logmanager.service;

import jp.co.canon.rss.logmanager.dto.Upload.ResLocalJobFileIdx;
import jp.co.canon.rss.logmanager.repository.LocalJobFileIdVoRepository;
import jp.co.canon.rss.logmanager.repository.LocalJobRepository;
import jp.co.canon.rss.logmanager.repository.RemoteJobRepository;
import jp.co.canon.rss.logmanager.system.FileUploadDownloadService;
import jp.co.canon.rss.logmanager.vo.LocalJobFileIdVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Slf4j
@Service()
public class UploadService {
    LocalJobFileIdVoRepository localJobFileIdVoRepository;
    FileUploadDownloadService fileUploadDownloadService;

    public UploadService(LocalJobFileIdVoRepository localJobFileIdVoRepository,
                         FileUploadDownloadService fileUploadDownloadService) {
        this.localJobFileIdVoRepository = localJobFileIdVoRepository;
        this.fileUploadDownloadService = fileUploadDownloadService;
    }

    public ResLocalJobFileIdx uploadLocalJobFile(MultipartFile file) throws FileUploadException {
        final String format = "%s_%s";
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String savedFileName = String.format(format, currentTime, file.getOriginalFilename());  // 20210518_filename.zip
        fileUploadDownloadService.storeLocalFile(file, savedFileName);

        LocalJobFileIdVo localJobFileIdVo = new LocalJobFileIdVo()
                .setUploadDate(LocalDateTime.now())
                .setFileName(savedFileName)
                .setFileOriginalName(file.getOriginalFilename());

        ResLocalJobFileIdx resLocalJobFileIdx = new ResLocalJobFileIdx()
                .setFileIndex(localJobFileIdVoRepository.save(localJobFileIdVo).getId());

        return resLocalJobFileIdx;
    }
}
