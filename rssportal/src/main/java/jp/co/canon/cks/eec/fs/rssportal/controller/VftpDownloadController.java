package jp.co.canon.cks.eec.fs.rssportal.controller;

import jp.co.canon.ckbs.eec.fs.collect.controller.param.VFtpSssListRequestResponse;
import jp.co.canon.ckbs.eec.fs.collect.model.VFtpSssListRequest;
import jp.co.canon.ckbs.eec.fs.collect.service.VFtpFileInfo;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnectorFactory;
import jp.co.canon.cks.eec.fs.rssportal.Defines.RSSErrorReason;
import jp.co.canon.cks.eec.fs.rssportal.background.*;
import jp.co.canon.cks.eec.fs.rssportal.background.search.FileSearchException;
import jp.co.canon.cks.eec.fs.rssportal.background.search.FileSearchInfo;
import jp.co.canon.cks.eec.fs.rssportal.background.search.FileSearchManager;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import jp.co.canon.cks.eec.fs.rssportal.common.LogType;
import jp.co.canon.cks.eec.fs.rssportal.common.Tool;
import jp.co.canon.cks.eec.fs.rssportal.model.DownloadStatusResponseBody;
import jp.co.canon.cks.eec.fs.rssportal.model.auth.AccessToken;
import jp.co.canon.cks.eec.fs.rssportal.model.error.RSSError;
import jp.co.canon.cks.eec.fs.rssportal.model.vftp.VFtpFileInfoExtends;
import jp.co.canon.cks.eec.fs.rssportal.model.vftp.VFtpSssListRequestResponseExtends;
import jp.co.canon.cks.eec.fs.rssportal.service.DownloadHistoryService;
import jp.co.canon.cks.eec.fs.rssportal.service.JwtService;
import jp.co.canon.cks.eec.fs.rssportal.service.UserService;
import jp.co.canon.cks.eec.fs.rssportal.vo.DownloadHistoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/vftp")
public class VftpDownloadController extends DownloadControllerCommon {
    private final EspLog log = new EspLog(getClass());

    @Value("${rssportal.file-collect-service.retry}")
    private int fileServiceRetryCount;

    @Value("${rssportal.file-collect-service.retry-interval}")
    private int fileServiceRetryInterval;

    @Value("${rssportal.file-service-manager.addr}")
    private String fileServiceAddress;

    private final DownloadHistoryService dlHistory;
    private final UserService userService;
    private final FileSearchManager fileSearchManager;
    public static final String TOKEN_PREFIX = "Bearer ";

    @Autowired
    public VftpDownloadController(JwtService jwtService, FileDownloader fileDownloader,
                                  FileServiceManageConnectorFactory connectorFactory,
                                  DownloadHistoryService dlHistory,
                                  UserService userService,
                                  FileSearchManager fileSearchManager) {
        super(jwtService, fileDownloader, connectorFactory);
        this.dlHistory = dlHistory;
        this.userService = userService;
        this.fileSearchManager = fileSearchManager;
    }

    // Request VFTP Comapt Downlaod
    @PostMapping("/compat/download")
    @ResponseBody
    public ResponseEntity<?> vftpCompatDownloadRequest(HttpServletRequest request, @RequestBody Map<String, Object> param) {
        log.info(String.format("[Post] %s", request.getServletPath()), LogType.control);
        RSSError error = new RSSError();
        Map<String, Object> resBody = new HashMap<>();

        if(param==null) {
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
        }

        // check permission
        if(!Tool.isVftpUser(Tool.toJavaList(jwtService.getCurAccTokenUserPermission()))) {
            error.setReason(RSSErrorReason.INSUFFICIENT_PERMISSIONS);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resBody);
        }

        List<String> fabs = param.containsKey("fabNames")?(List)param.get("fabNames"):null;
        List<String> machines = param.containsKey("machineNames")?(List)param.get("machineNames"):null;
        String command = param.containsKey("command") ? (String) param.get("command") : null;

        if(fabs==null || machines==null || fabs.size()==0 || fabs.size()!=machines.size() || command==null) {
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
        }

        List<DownloadRequestForm> list = new ArrayList<>();
        for(int i=0; i<machines.size(); ++i) {
            VFtpCompatDownloadRequestForm form = new VFtpCompatDownloadRequestForm(fabs.get(i), machines.get(i), command, true);
            list.add(form);
        }

        String downloadId = fileDownloader.addRequest(JobType.manual, CollectType.vftp_compat, list);
        log.info("vftp-compat downloadId="+downloadId, LogType.control);
        resBody.put("downloadId", downloadId);
        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    // Cancel VFTP Comapt Download
    @DeleteMapping({"/compat/download/{downloadId}", "/sss/download/{downloadId}"})
    @ResponseBody
    public ResponseEntity<?> vftpCompatDownloadCancel(HttpServletRequest request, @PathVariable("downloadId") String downloadId) {
        log.info(String.format("[Delete] %s", request.getServletPath()), LogType.control);
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        if (downloadId == null) {
            log.warn("downloadId is null", LogType.control);
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        // check permission
        if(!Tool.isVftpUser(Tool.toJavaList(jwtService.getCurAccTokenUserPermission()))) {
            error.setReason(RSSErrorReason.INSUFFICIENT_PERMISSIONS);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resBody);
        }

        if(!fileDownloader.cancelRequest(downloadId)) {
            log.warn("downloadId is invalid", LogType.control);
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    // Request VFTP Compat Download status
    @GetMapping({"/compat/download/{downloadId}", "/sss/download/{downloadId}"})
    @ResponseBody
    public ResponseEntity<?> vftpCompatDownloadStatus(HttpServletRequest request, @PathVariable("downloadId") String downloadId) {
        log.info(String.format("[Get] %s", request.getServletPath()), LogType.control);
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        if(downloadId == null) {
            log.warn("downloadId is null", LogType.control);
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        // check permission
        if(!Tool.isVftpUser(Tool.toJavaList(jwtService.getCurAccTokenUserPermission()))) {
            error.setReason(RSSErrorReason.INSUFFICIENT_PERMISSIONS);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resBody);
        }

        if(fileDownloader.isValidId(downloadId)==false) {
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        return ResponseEntity.status(HttpStatus.OK).body(new DownloadStatusResponseBody(fileDownloader, downloadId));
    }

    @GetMapping({"/compat/validation/{downloadId}", "/sss/validation/{downloadId}"})
    @ResponseBody
    public ResponseEntity<?> isVftpCompatDownloadFile(
        @PathVariable("downloadId") String downloadId,
        HttpServletRequest request,
        HttpServletResponse response) {
        log.info(String.format("[Get] %s", request.getServletPath()), LogType.control);
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        if(downloadId == null) {
            log.error("invalid param", LogType.control);
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        // check permission
        if(!Tool.isVftpUser(Tool.toJavaList(jwtService.getCurAccTokenUserPermission()))) {
            error.setReason(RSSErrorReason.INSUFFICIENT_PERMISSIONS);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resBody);
        }

        if(fileDownloader.isValidId(downloadId)==false) {
            log.error("invalid dlId", LogType.control);
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }
        if(fileDownloader.getStatus(downloadId).equals("done")==false) {
            log.error("in-progress", LogType.control);
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    // Download VFP Compat file
    @GetMapping({"/compat/storage/{downloadId}", "/sss/storage/{downloadId}"})
    @ResponseBody
    public ResponseEntity<?> vftpCompatDownloadFile(
            @PathVariable("downloadId") String downloadId,
            @RequestParam(name="accesstoken", required = false, defaultValue = "") String accesstoken,
            HttpServletRequest request,
            HttpServletResponse response) {
        log.info(String.format("[Get] %s", request.getServletPath()), LogType.control);
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();
        AccessToken decodedAccess = null;

        String headerAccessToken = request.getHeader("Authorization");
        if(jwtService.isUsable(headerAccessToken)
            && !userService.getToken(headerAccessToken.replace(TOKEN_PREFIX, ""))) {
            decodedAccess = jwtService.decodeAccessToken(request.getHeader("Authorization"));
        } else {
            if(jwtService.isUsable(accesstoken)
                && !userService.getToken(accesstoken.replace(TOKEN_PREFIX, ""))) {
                decodedAccess = jwtService.decodeAccessToken(accesstoken);
            } else {
                error.setReason(RSSErrorReason.INVALID_ACCESS_TOKEN);
                resBody.put("error", error.getRSSError());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resBody);
            }
        }

        // check permission
        if(!Tool.isVftpUser(Tool.toJavaList(decodedAccess.getPermission()))) {
            error.setReason(RSSErrorReason.INSUFFICIENT_PERMISSIONS);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resBody);
        }

        DownloadHistoryVo history = new DownloadHistoryVo();
        history.setDl_user(decodedAccess.getUserName());
        /*RSS_TYPE_VFTP_MANUAL_COMPAT = 3;  RSS_TYPE_VFTP_MANUAL_SSS = 4;*/
        history.setDl_type(request.getServletPath().contains("compat") ? "3": "4");

        if(downloadId == null) {
            log.error("invalid param", LogType.control);
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        if(fileDownloader.isValidId(downloadId)==false) {
            log.error("invalid dlId", LogType.control);
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            history.setDl_status("Download Failed (file not found)");
            dlHistory.addDlHistory(history);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }
        if(fileDownloader.getStatus(downloadId).equals("done")==false) {
            log.error("in-progress", LogType.control);
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            history.setDl_status("Download Failed (in-progress)");
            dlHistory.addDlHistory(history);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        String dlPath = fileDownloader.getDownloadInfo(downloadId);
        log.info("download path="+dlPath, LogType.control);

        try {
            InputStream is = new FileInputStream(new File(dlPath));
            InputStreamResource isr = new InputStreamResource(is);
            HttpHeaders headers = new HttpHeaders();
            String dlFilename = createZipFilename(downloadId, decodedAccess.getUserName());

            headers.setContentLength(Files.size(Paths.get(dlPath)));
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            response.setHeader("Content-Disposition", "attachment; filename="+dlFilename);

            history.setDl_filename(dlFilename);
            history.setDl_status("Download Completed");
            dlHistory.addDlHistory(history);
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(isr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        error.setReason(RSSErrorReason.NOT_FOUND);
        resBody.put("error", error.getRSSError());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
    }

    private ArrayList<VFtpSssListRequestResponseExtends> searchVftpSSSRequest(ArrayList<String> fabNames,
                                                                              ArrayList<String> machineNames,
                                                                              String command) throws Exception {
        ArrayList<VFtpSssListRequestResponseExtends> requestList = new ArrayList<VFtpSssListRequestResponseExtends>();
        boolean done = false;
        int retry = 0;
        for(int i=0; i< machineNames.size(); i++) {
            retry = 0;
            while (retry < fileServiceRetryCount) {
                try {
                    VFtpSssListRequestResponse responseInfo =
                            (VFtpSssListRequestResponse)connectorFactory.getConnector(fileServiceAddress).createVFtpSssListRequest(
                                    machineNames.get(i),
                                    command);
                    VFtpSssListRequestResponseExtends newResponse = new VFtpSssListRequestResponseExtends();
                    newResponse.setFabName(fabNames.get(i));
                    newResponse.setErrorCode(responseInfo.getErrorCode());
                    newResponse.setErrorMessage(responseInfo.getErrorMessage());
                    newResponse.setRequest(responseInfo.getRequest());
                    requestList.add(newResponse);
                    done = true;
                    break;
                } catch (Exception e) {
                    retry++;
                    log.error("searchVftpSSSRequest" + " : request failed(retry: " + retry + ")", LogType.control);
                    log.error("searchVftpSSSRequest" + " : " + e, LogType.control);
                    Thread.sleep(fileServiceRetryInterval);
                }
            }
        }
        if(!done) {
            log.error("searchVftpSSSRequest : An error occurred for request.", LogType.control);
        }
        return requestList;
    }

    private ArrayList<VFtpFileInfoExtends> searchVftpSSSResponse(ArrayList<VFtpSssListRequestResponseExtends> requestList) throws Exception {
        ArrayList<VFtpFileInfoExtends> responseList = new ArrayList<VFtpFileInfoExtends>();
        int listCnt = requestList.size();
        int doneCnt = 0;
        int errCnt = 0;
        ArrayList<VFtpSssListRequestResponseExtends> newRequestList = new ArrayList<VFtpSssListRequestResponseExtends>();
        newRequestList.addAll(requestList);
        while (doneCnt < listCnt) {
            for (Iterator<VFtpSssListRequestResponseExtends> iter = newRequestList.iterator(); iter.hasNext(); ) {
                int retry = 0;
                VFtpSssListRequestResponseExtends req = iter.next();
                VFtpSssListRequestResponse res = null;
                while (retry < fileServiceRetryCount) {
                    try {
                         res = connectorFactory.getConnector(fileServiceAddress).getVFtpSssListRequest(
                                req.getRequest().getMachine(), req.getRequest().getRequestNo());
                         break;
                    } catch (Exception e) {
                        retry++;
                        log.error("searchVftpSSSResponse" + " : request failed(retry: " + retry + ")", LogType.control);
                        log.error("searchVftpSSSResponse" + " : " + e, LogType.control);
                        Thread.sleep(fileServiceRetryInterval);
                    }
                }
                if(res == null) {
                    iter.remove();
                    doneCnt++;
                    errCnt++;
                } else {
                    VFtpSssListRequest.Status status = res.getRequest().getStatus();
                    if (status == VFtpSssListRequest.Status.ERROR) {
                        iter.remove();
                        doneCnt++;
                        errCnt++;
                    } else if (status == VFtpSssListRequest.Status.CANCEL) {
                        iter.remove();
                        doneCnt++;
                    } else if (status == VFtpSssListRequest.Status.EXECUTED) {
                        for (VFtpFileInfo file : res.getRequest().getFileList()) {
                            VFtpFileInfoExtends convFile = new VFtpFileInfoExtends();
                            convFile.setFabName(req.getFabName());
                            convFile.setMachineName(res.getRequest().getMachine());
                            convFile.setCommand(res.getRequest().getDirectory());
                            convFile.setFileName(file.getFileName());
                            convFile.setFileType(file.getFileType());
                            convFile.setFileSize(file.getFileSize());
                            responseList.add(convFile);
                        }
                        iter.remove();
                        doneCnt++;
                    }
                    if (doneCnt < listCnt) {
                        Thread.yield();
                    }
                }
            }
        }
        if (listCnt == errCnt) {
            log.error("searchVftpSSSResponse : An error occurred for every request.", LogType.control);
        }
        return responseList;
    }

    // Search VFTP SSS File
    @PostMapping("/sss")
    @ResponseBody
    public ResponseEntity<?> searchVftpSSSFileList(HttpServletRequest request, @RequestBody Map<String, Object> param) throws Exception {
        String requestUrl = String.format("[Post] %s", request.getServletPath(), LogType.control);
        log.info(requestUrl);
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        if(param == null) {
            log.info(requestUrl + " : no param", LogType.control);
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
        }

        // check permission
        if(!Tool.isVftpUser(Tool.toJavaList(jwtService.getCurAccTokenUserPermission()))) {
            error.setReason(RSSErrorReason.INSUFFICIENT_PERMISSIONS);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resBody);
        }

        ArrayList<String> fabNames = param.containsKey("fabNames") ? (ArrayList<String>) param.get("fabNames") : null;
        ArrayList<String> machineNames = param.containsKey("machineNames") ? (ArrayList<String>) param.get("machineNames") : null;
        String command = param.containsKey("command") ? (String) param.get("command") : null;

        if(fabNames == null || fabNames.size() == 0 || machineNames == null  || machineNames.size() == 0|| command == null) {
            log.error(requestUrl + " : parameter is not matched", LogType.control);
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
        }

        if(fabNames.size() != machineNames.size()) {
            log.error(requestUrl + " : fabNames.size() and machineNames.size() are not the same.", LogType.control);
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
        }

        ArrayList<VFtpSssListRequestResponseExtends> requestList = searchVftpSSSRequest(fabNames, machineNames, command);
        ArrayList<VFtpFileInfoExtends> responseList = null;
        if(requestList.size() != 0) {
            responseList = searchVftpSSSResponse(requestList);

        }
        resBody.put("lists", responseList);
        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    // Request VFTP SSS Downlaod
    @PostMapping("/sss/download")
    @ResponseBody
    public ResponseEntity<?> vftpSSSDownloadRequest(HttpServletRequest request, @RequestBody Map<String, Object> param) {
        log.info(String.format("[Post] %s", request.getServletPath()), LogType.control);
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        // check permission
        if(!Tool.isVftpUser(Tool.toJavaList(jwtService.getCurAccTokenUserPermission()))) {
            error.setReason(RSSErrorReason.INSUFFICIENT_PERMISSIONS);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resBody);
        }

        List<Map<String, Object>> requestList = (List<Map<String, Object>>) param.get("lists");
        List<DownloadRequestForm> list = new ArrayList<>();

        loop_top:
        for(Map item: requestList) {
            String fabName = (String) item.get("fabName");
            String machineName = (String) item.get("machineName");
            String directory = (String) item.get("command");
            String fileName = (String) item.get("fileName");
            long fileSize = ((Number)item.get("fileSize")).longValue();

            for(DownloadRequestForm _form: list) {
                VFtpSssDownloadRequestForm form = (VFtpSssDownloadRequestForm)_form;
                if(form.getMachine().equals(machineName) && form.getFab().equals(fabName) && form.getDirectory().equals(directory)) {
                    form.addFile(fileName, fileSize);
                    continue loop_top;
                }
            }
            VFtpSssDownloadRequestForm form = new VFtpSssDownloadRequestForm(fabName, machineName, directory);
            form.addFile(fileName, fileSize);
            list.add(form);
        }

        String downloadId = fileDownloader.addRequest(JobType.manual, CollectType.vftp_sss, list);
        log.info("vftp-sss downloadId="+downloadId, LogType.control);
        resBody.put("downloadId", downloadId);
        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    @PostMapping("/sss/search")
    public ResponseEntity postVFtpSssFileSearch(HttpServletRequest request, @RequestBody Map<String, Object> body) {

        log.info("POST "+request.getServletPath(), LogType.control);
        Map<String, Object> responseBody = new HashMap<>();

        if(body!=null) {
            List<String> fabNames = getObjectFromMap(body, "fabNames", new ArrayList<>());
            List<String> machineNames = getObjectFromMap(body, "machineNames", new ArrayList<>());
            String command = getObjectFromMap(body, "command", new String());

            if(fabNames!=null && fabNames.size()!=0 && machineNames!=null && machineNames.size()!=0 && command!=null) {
                try {
                    String searchId = fileSearchManager.requestVFtpSearch(
                            fabNames.toArray(new String[0]),
                            machineNames.toArray(new String[0]),
                            command);
                    if(searchId==null) {
                        // FileSearchManager might be uninitialized now.
                        responseBody.put("error", new RSSError(RSSErrorReason.INTERNAL_ERROR));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
                    }

                    responseBody.put("searchId", searchId);
                    return ResponseEntity.status(HttpStatus.OK).body(responseBody);

                } catch (FileSearchException e) {
                    String msg = e.getMessage();
                    if(msg.equals(FileSearchException.Error.jobFull)) {
                        responseBody.put("error", new RSSError(RSSErrorReason.INTERNAL_ERROR));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
                    }
                }
            }
        }

        responseBody.put("error", new RSSError(RSSErrorReason.INVALID_PARAMETER));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    @GetMapping("/sss/search/{searchId}")
    public ResponseEntity getVFtpSssFileSearch(HttpServletRequest request, @PathVariable String searchId) {

        log.info("GET "+request.getServletPath()+"/"+searchId, LogType.control);
        Map<String, Object> responseBody = new HashMap<>();

        FileSearchInfo info = fileSearchManager.getSearchInfo(searchId);
        if(info==null) {
            responseBody.put("error", new RSSError(RSSErrorReason.NOT_FOUND));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
        }
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }

    @DeleteMapping("/sss/search/{searchId}")
    public ResponseEntity deleteVFtpSssFileSearch(HttpServletRequest request, @PathVariable String searchId) {

        log.info("DELETE "+request.getServletPath()+"/"+searchId, LogType.control);
        fileSearchManager.cancelJob(searchId);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/sss/search/result/{searchId}")
    public ResponseEntity getVFtpSssFileSearchResult(HttpServletRequest request, @PathVariable String searchId) {

        log.info("GET "+request.getServletPath()+"/"+searchId, LogType.control);
        Map<String, Object> responseBody = new HashMap<>();

        List<VFtpFileInfoExtends> files = fileSearchManager.getSearchedFileList(searchId);

        if(files==null) {
            responseBody.put("error", new RSSError(RSSErrorReason.NOT_FOUND));
            return new ResponseEntity(responseBody, HttpStatus.NOT_FOUND);
        }

        responseBody.put("lists", files);
        return new ResponseEntity(responseBody, HttpStatus.OK);
    }

    /*
    // Cancel VFTP SSS Download
    @DeleteMapping("/sss/download/{downloadId}")
    @ResponseBody
    public ResponseEntity<?> vftpSSSDownloadCancel(HttpServletRequest request, @PathVariable("downloadId") String downloadId) {
        log.info(String.format("[Delete] %s", request.getServletPath()));
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        if(!fileDownloader.cancelRequest(downloadId)) {
            log.warn("downloadId is invalid");
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    // Request VFTP SSS Download status
    @GetMapping("/sss/download/{downloadId}")
    @ResponseBody
    public ResponseEntity<?> vftpSSSDownloadStatus(HttpServletRequest request, @PathVariable("downloadId") String downloadId) {
        log.info(String.format("[Get] %s", request.getServletPath()));
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();
        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    // Download VFP SSS file
    @RequestMapping("/sss/storage/{downloadId}")
    public ResponseEntity<?> vftpSSSDownloadFile(
            @PathVariable("downloadId") String downloadId,
            HttpServletRequest request,
            HttpServletResponse response) {
        log.info(String.format("[Get] %s", request.getServletPath()));
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();
        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }
     */
}
