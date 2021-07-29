package jp.co.canon.rss.logmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jp.co.canon.rss.logmanager.config.ReqURLController;
import jp.co.canon.rss.logmanager.dto.Upload.ResLocalJobFileIdx;
import jp.co.canon.rss.logmanager.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping(ReqURLController.API_DEFAULT_UPLOAD_URL)
public class UploadController {
    UploadService uploadService;
    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    // local file 업로드
    @PostMapping(value = ReqURLController.API_POST_LOCALFILE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary="Log file registration for Local Job registration")
    @ApiResponses({
            @ApiResponse(
                    responseCode="200",
                    description="OK(Log file upload success and automatically generated file ID return)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = "{\n  \"fileIndex\": 1\n}"))
            ),
            @ApiResponse(responseCode="400", description="Bad Request"),
            @ApiResponse(responseCode="404", description="Not Found"),
            @ApiResponse(responseCode="500", description="Internal Server Error")
    })
    public ResponseEntity<?> uploadFile(
            HttpServletRequest request,
            @Parameter(
                    name = "file",
                    description = "Local Log file to be analyzed(MultipartFile)",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = MultipartFile.class))
            )
            @RequestParam("file") MultipartFile file) {
        try {
            ResLocalJobFileIdx buildLogList = uploadService.uploadLocalJobFile(file);
            return ResponseEntity.status(HttpStatus.OK).body(buildLogList);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
