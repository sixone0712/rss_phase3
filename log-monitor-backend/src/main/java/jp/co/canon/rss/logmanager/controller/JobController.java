package jp.co.canon.rss.logmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jp.co.canon.rss.logmanager.config.ReqURLController;
import jp.co.canon.rss.logmanager.controller.examples.JobExamples;
import jp.co.canon.rss.logmanager.controller.model.job.ReqLocalJob;
import jp.co.canon.rss.logmanager.controller.model.job.ReqRemoteJob;
import jp.co.canon.rss.logmanager.controller.model.job.ResRemoteJobDetail;
import jp.co.canon.rss.logmanager.dto.job.*;
import jp.co.canon.rss.logmanager.exception.StatusResourceNotFoundException;
import jp.co.canon.rss.logmanager.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(ReqURLController.API_DEFAULT_JOB_URL)
public class JobController {
    private JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // remote job status list 취득
    @GetMapping(ReqURLController.API_GET_REMOTE_JOB_LIST)
    @Operation(summary = "Get status list for all Remote Jobs")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK(Successful status list acquisition for all Remote Jobs)",
                    content = @Content(
                            schema = @Schema(implementation = ResRemoteJobListDTO.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "example1",
                                    value = JobExamples.RES_GET_REMOTE_JOB_LIST))
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<?> getRemoteJobList(HttpServletRequest request) {
        try {
            List<ResRemoteJobListDTO> remoteJobs = jobService.getRemoteJobs();
            return ResponseEntity.status(HttpStatus.OK).body(remoteJobs);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // remote job status 세부 정보 취득
    @GetMapping(ReqURLController.API_GET_REMOTE_JOB_DETAIL)
    @Operation(summary = "Get detailed status information for the specified Remote Job")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK(Successful acquisition of Job details)",
                    content = @Content(
                            schema = @Schema(implementation = ResRemoteJobDetail.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "example1",
                                    value = JobExamples.RES_GET_REMOTE_JOB_DETAIL))

            ),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<?> getRemoteJobDetail(
            HttpServletRequest request,
            @Parameter(name = "id", description = "Job ID to get detailed information", required = true, example = "10")
            @Valid @PathVariable(value = "id") @NotNull int remoteJobId) {
        try {
            ResRemoteJobDetailDTO resRemoteJobDetail = jobService.getRemoteJobDetail(remoteJobId);
            return ResponseEntity.status(HttpStatus.OK).body(resRemoteJobDetail);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // remote job 추가
    @PostMapping(ReqURLController.API_POST_NEW_REMOTE_JOB)
    @Operation(summary = "Add new Remote Job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK(Remote Job added successfully)"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<?> addRemoteJob(
            HttpServletRequest request,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Remote job data to add the information",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ReqRemoteJob.class),
                            examples = @ExampleObject(value = JobExamples.REQ_ADD_EDIT_REMOTE_JOB))
            )
            @RequestBody ResRemoteJobDetailAddDTO resRemoteJobDetailDTO) {
        try {
            ResJobIdDTO addRemoteJobId = jobService.addRemoteJob(resRemoteJobDetailDTO);
            return ResponseEntity.status(HttpStatus.OK).body(addRemoteJobId);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // remote job 삭제
    @DeleteMapping(ReqURLController.API_DELETE_REMOTE_JOB)
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @Operation(summary = "Delete specified Remote Job (Delete related Notification information and MailContext information at the same time)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK(Successful Job deletion)"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<?> deleteRemoteJob(
            HttpServletRequest request,
            @Parameter(name = "id", description = "Job ID to delete", required = true, example = "1")
            @Valid @PathVariable(value = "id") @NotNull int remoteJobId) {
        try {
            jobService.deleteRemoteJob(remoteJobId);
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // remote job 수정
    @PutMapping(ReqURLController.API_PUT_REMOTE_JOB)
    @Operation(summary = "Modify the information of the already registered Remote Job")
    @Parameters({
            @Parameter(name = "id", description = "Job ID to modify the information", required = true)
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK(Successful Job information modify)"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<?> updateJob(
            HttpServletRequest request,
            @Parameter(name = "id", description = "Job ID to modify the information", required = true, example = "3")
            @Valid @PathVariable(value = "id") @NotNull int jobId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Remote job modify to add the information",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ReqRemoteJob.class),
                            examples = @ExampleObject(value = JobExamples.REQ_ADD_EDIT_REMOTE_JOB))
            )
            @Parameter(
                    name = "jsonObj",
                    description = "Remote job data to modify the information",
                    required = true,
                    schema = @Schema(implementation = ReqRemoteJob.class),
                    example = JobExamples.REQ_ADD_EDIT_REMOTE_JOB
            )
            @RequestBody ResRemoteJobDetailAddDTO resRemoteJobDetailDTO) {
        try {
            ResJobIdDTO editRemoteJobId = jobService.editRemoteJob(jobId, resRemoteJobDetailDTO);
            return ResponseEntity.status(HttpStatus.OK).body(editRemoteJobId);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // remote job stop status
    @GetMapping(ReqURLController.API_GET_REMOTE_JOB_STATUS)
    @Operation(summary = "Get stop status of registered Remote Job")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "stopped",
                                            description = "job is stopped",
                                            value = "{\n  \"stop\": \"true\"\n}"),
                                    @ExampleObject(
                                            name = "running",
                                            description = "job is running",
                                            value = "{\n  \"stop\": \"false\"\n}")
                            })),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    })
    public ResponseEntity<?> getStatusRemoteJob(
            HttpServletRequest request,
            @Parameter(name = "id", description = "Job ID to start log analysis", required = true, example = "1")
            @Valid @PathVariable(value = "id") @NotNull int jobId) {
        try {
            ResRemoteJobStatusDTO resRemoteJobStatusDTO = jobService.getStatusRemoteJob(jobId);
            return ResponseEntity.status(HttpStatus.OK).body(resRemoteJobStatusDTO);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // remote job 시작(stop:false)
    @PatchMapping(ReqURLController.API_PATCH_REMOTE_JOB_RUN)
    @Operation(summary = "Start log analysis of already registered Remote Job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK(Successful start of log analysis)"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<?> runRemoteJob(
            HttpServletRequest request,
            @Parameter(name = "id", description = "Job ID to start log analysis", required = true, example = "1")
            @Valid @PathVariable(value = "id") @NotNull int jobId) {
        try {
            ResJobIdDTO resJobIdDTO = jobService.runStopRemoteJob(jobId, "run");
            return ResponseEntity.status(HttpStatus.OK).body(resJobIdDTO);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // remote job 정지(stop:true)
    @PatchMapping(ReqURLController.API_PATCH_REMOTE_JOB_STOP)
    @Operation(summary = "Stop log analysis of already registered Remote Job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK(Successful log analysis cancellation)"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<?> stopRemoteJob(
            HttpServletRequest request,
            @Parameter(name = "id", description = "Job ID to stop log analysis", required = true, example = "1")
            @Valid @PathVariable(value = "id") @NotNull int jobId) {
        try {
            ResJobIdDTO resJobIdDTO = jobService.runStopRemoteJob(jobId, "stop");
            return ResponseEntity.status(HttpStatus.OK).body(resJobIdDTO);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // local job status 리스트 취득
    @GetMapping(ReqURLController.API_GET_LOCAL_JOB_LIST)
    @Operation(summary = "Get status list for all Local Jobs")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK(Successful acquisition of status list for all Local Jobs)",
                    content = @Content(
                            schema = @Schema(implementation = ResLocalJobListDTO.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "example1",
                                    value = JobExamples.RES_GET_LOCAL_JOB_LIST))

            ),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<?> getLocalJobList(HttpServletRequest request) {
        try {
            List<ResLocalJobListDTO> resultLocalList = jobService.localJobListDTOS();
            return ResponseEntity.status(HttpStatus.OK).body(resultLocalList);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // local job 추가
    @PostMapping(ReqURLController.API_POST_NEW_LOCAL_JOB)
    @Operation(summary = "New Local Job registration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK(Successful registration of new Local Job)"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<?> setLocalJob(
            HttpServletRequest request,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Local job data to add the information",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ReqLocalJob.class),
                            examples = @ExampleObject(value = "{\n  \"siteId\": 10,\n  \"fileIndices\": [\n    64,\n    65\n  ]\n}"))
            )
            @RequestBody ResLocalJobListDTO resLocalJobListDTO) {
        try {
            ResJobIdDTO resJobIdDTO = jobService.addLocalJob(resLocalJobListDTO);
            return ResponseEntity.status(HttpStatus.OK).body(resJobIdDTO);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // local job 삭제
    @DeleteMapping(ReqURLController.API_DELETE_LOCAL_JOB)
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @Operation(summary = "Delete specified Local Job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK(Successful Job deletion)"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<?> deleteLocalJob(
            HttpServletRequest request,
            @Parameter(name = "id", description = "Job ID to delete", required = true, example = "1")
            @Valid @PathVariable(value = "id") @NotNull int localJobId) throws StatusResourceNotFoundException {
        try {
            jobService.deleteLocalJob(localJobId);
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
