package jp.co.canon.cks.eec.fs.rssportal.controller;

import jp.co.canon.ckbs.eec.fs.collect.service.FileInfo;
import jp.co.canon.ckbs.eec.fs.collect.service.LogFileList;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnectorFactory;
import jp.co.canon.cks.eec.fs.rssportal.Defines.RSSErrorReason;
import jp.co.canon.cks.eec.fs.rssportal.background.*;
import jp.co.canon.cks.eec.fs.rssportal.background.search.FileSearchException;
import jp.co.canon.cks.eec.fs.rssportal.background.search.FileSearchInfo;
import jp.co.canon.cks.eec.fs.rssportal.background.search.FileSearchManager;
import jp.co.canon.cks.eec.fs.rssportal.common.LogType;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import jp.co.canon.cks.eec.fs.rssportal.model.DownloadStatusResponseBody;
import jp.co.canon.cks.eec.fs.rssportal.model.auth.AccessToken;
import jp.co.canon.cks.eec.fs.rssportal.model.error.RSSError;
import jp.co.canon.cks.eec.fs.rssportal.model.ftp.RSSFtpSearchRequest;
import jp.co.canon.cks.eec.fs.rssportal.model.ftp.RSSFtpSearchResponse;
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
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/api/ftp")
public class FileDownloaderController extends DownloadControllerCommon {
    private final EspLog log = new EspLog(getClass());

    @Value("${rssportal.file-service-manager.addr}")
    private String fileServiceAddress;

    private final DownloadHistoryService dlHistory;
    private final UserService userService;
    public static final String TOKEN_PREFIX = "Bearer ";

    @Autowired
    private FileSearchManager fileSearchManager;

    @Autowired
    public FileDownloaderController(FileDownloader fileDownloader, JwtService jwtService,
                                    FileServiceManageConnectorFactory connectorFactory,
                                    DownloadHistoryService serviceDlHistory,
                                    UserService userService) {
        super(jwtService, fileDownloader, connectorFactory);
        this.dlHistory = serviceDlHistory;
        this.userService = userService;
    }

    private boolean createFileList(List<RSSFtpSearchResponse> list, RSSFtpSearchRequest request, int depth) {
        if(list==null || request==null) return false;

        int fileServiceRetryCount = fileDownloader.getFileServiceRetryCount();
        int fileServiceRetryInterval = fileDownloader.getFileServiceRetryInterval();

        LogFileList fileInfo = null;
        int retry = 0;
        while(retry<fileServiceRetryCount) {
            try {
                fileInfo = connectorFactory.getConnector(fileServiceAddress).getFtpFileList(request.getMachineName(),
                        request.getCategoryCode(),
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getKeyword(),
                        request.getDir());

                if(fileInfo.getErrorMessage()!=null) {
                    log.error("failed to get file-list  err="+fileInfo.getErrorMessage());
                    return false;
                }

                for(FileInfo file: fileInfo.getList()) {
                    if(file.getType().equals("D") && depth<request.getDepth()) {
                        // Search for files in a directory only when the directory date falls within the search date range
                        // FTP Folder Date is UTC
                        /*
                        long dirTimestamp = Long.parseLong(file.getTimestamp());
                        log.info("dirTimestamp: " + dirTimestamp);
                        log.info("file.getFilename(): " + file.getFilename());
                        long searchFrom = Long.parseLong(request.getStartDate());
                        long searchTo = Long.parseLong(request.getEndDate());
                        if(dirTimestamp > searchTo || dirTimestamp < searchFrom) continue;
                        */
                        if(file.getFilename().endsWith("/.") || file.getFilename().endsWith("/..")) {
                            continue;
                        }
                        if(!fileDownloader.isBetween(file.getTimestamp(), request.getStartDate(), request.getEndDate())) {
                            continue;
                        }
                        RSSFtpSearchRequest child = request.getClone();
                        child.setDir(file.getFilename());
                        if(!createFileList(list, child, depth+1)) {
                            log.warn(String.format("[createFileList]connection error (%s %s %s)",
                                    request.getMachineName(), request.getCategoryCode(), request.getDir()));
                        }
                    } else {
                        if(file.getFilename().endsWith(".") || file.getFilename().endsWith("..") ||
                                (file.getType().equals("F") && file.getSize()==0))
                            continue;

                        RSSFtpSearchResponse info = new RSSFtpSearchResponse();
                        info.setCategoryCode(request.getCategoryCode());
                        info.setFileName(file.getFilename());
                        String[] paths = file.getFilename().split("/");
                        if(paths.length>1) {
                            int lastIndex = file.getFilename().lastIndexOf("/");
                            info.setFilePath(file.getFilename().substring(0, lastIndex));
                        } else {
                            info.setFilePath(".");
                        }
                        info.setFileSize(file.getSize());
                        info.setFileDate(file.getTimestamp());
                        info.setFileType(file.getType());
                        info.setFabName(request.getFabName());
                        info.setMachineName(request.getMachineName());
                        info.setCategoryName(request.getCategoryName());
                        list.add(info);
                    }
                }
                return true;
            } catch (Exception e) {
                log.error("[createFileList]request failed(retry: " + (++retry) + ")");
                log.error("[createFileList]request failed: " + e);
                try {
                    Thread.sleep(fileServiceRetryInterval);
                } catch (InterruptedException interruptedException) {
                    log.error("[createFileList]failed to sleep");
                }
            }
        }
        return false;
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> searchFTPFileListWithThreadPool(HttpServletRequest request, @RequestBody Map<String, Object> requestList) throws Exception {
        log.info(String.format("[Post] %s", request.getServletPath()), LogType.control);

        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        if(requestList==null) {
            log.error("no param", LogType.control);
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
        }

        List<RSSFtpSearchResponse> responselists = new ArrayList<>();

        ArrayList<String> fabNames = requestList.containsKey("fabNames") ? (ArrayList<String>) requestList.get("fabNames") : null;
        ArrayList<String> machineNames = requestList.containsKey("machineNames") ? (ArrayList<String>) requestList.get("machineNames") : null;
        ArrayList<String> categoryCodes = requestList.containsKey("categoryCodes") ? (ArrayList<String>) requestList.get("categoryCodes") : null;
        ArrayList<String> categoryNames = requestList.containsKey("categoryNames") ? (ArrayList<String>) requestList.get("categoryNames") : null;
        String startDate = requestList.containsKey("startDate") ? (String) requestList.get("startDate") : null;
        String endDate = requestList.containsKey("endDate") ? (String) requestList.get("endDate") : null;
        int depth = 999;
        if(requestList.containsKey("depth")) {
            depth = ((Number)requestList.get("depth")).intValue();
        }

        if(fabNames == null || machineNames == null || categoryCodes == null || startDate == null || endDate == null) {
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
        }

        if((fabNames.size() != machineNames.size()) || (categoryCodes.size() != categoryNames.size())) {
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
        }

        if(startDate.equals("")) {
            startDate = "1970101000000";
        }
        if(endDate.equals("")) {
            endDate = "99991231235959";
        }

        /*
        log.info("fabNames: " + fabNames);
        log.info("machineNames: " + machineNames);
        log.info("categoryCodes: " + categoryCodes);
        log.info("startDate: " + startDate);
        log.info("endDate: " + endDate);
        */

        // Create ThreadPool with 10 threads
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        // Futrure object to hold the result when threads are executed asynchronously
        ArrayList<Future<ArrayList<RSSFtpSearchResponse>>> futures = new ArrayList<Future<ArrayList<RSSFtpSearchResponse>>>();

        for(int i = 0; i < machineNames.size(); i++) {
            for(int j = 0; j < categoryCodes.size(); j++) {
                RSSFtpSearchRequest searchRequest = new RSSFtpSearchRequest();
                searchRequest.setFabName(fabNames.get(i));
                searchRequest.setMachineName(machineNames.get(i));
                searchRequest.setCategoryCode(categoryCodes.get(j));
                searchRequest.setCategoryName(categoryNames.get(j));
                searchRequest.setStartDate(startDate);
                searchRequest.setEndDate(endDate);
                searchRequest.setDepth(depth);

                // Future object to hold the result when threads are executed asynchronously
                Callable<ArrayList<RSSFtpSearchResponse>> callable = new Callable<ArrayList<RSSFtpSearchResponse>>() {
                    @Override
                    public ArrayList<RSSFtpSearchResponse> call() throws Exception {
                        ArrayList<RSSFtpSearchResponse> result = new ArrayList<>();
                        if(!createFileList(result, searchRequest, 0)) {
                            log.warn("[searchFTPFileListWithThreadPool]failed to connect "+searchRequest.getMachineName());
                            return null;
                        }
                        return result;
                    }
                };
                futures.add(threadPool.submit(callable));
            }
        }
        threadPool.shutdown();

        int totalCnt = 0;
        int errCnt = 0;

        for (Future<ArrayList<RSSFtpSearchResponse>> future : futures) {
            totalCnt++;
            if(future.get() == null) {
                errCnt++;
            } else {
                responselists.addAll(future.get());
            }
        }

        if(totalCnt == errCnt) {
            log.error("[searchFTPFileListWithThreadPool]There is no response to all requests.", LogType.control);
        }

        resBody.put("lists", responselists);
        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    @PostMapping(value="/download")
    @ResponseBody
    public ResponseEntity<?> ftpDownloadRequest(HttpServletRequest request, @RequestBody Map<String, Object> param) {
        log.info(String.format("[Post] %s", request.getServletPath()), LogType.control);
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        if(param.size() == 0 || param.containsKey("lists") == false) {
            log.warn("no target to download", LogType.control);
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        //param.forEach((key, value)->log.info("key="+key+"\nvalue="+value));

        List<DownloadRequestForm> requestList = new ArrayList<>();
        Map<String, Map<String, FtpDownloadRequestForm>> map = new HashMap<>();
        List<Map<String, Object>> downloadList = (List<Map<String, Object>>) param.get("lists");

        for(Map item: downloadList) {
            String fabName = (String) item.get("fabName");
            String machineName = (String) item.get("machineName");
            String categoryCode = (String) item.get("categoryCode");
            String categoryName = (String) item.get("categoryName");
            String fileName = (String) item.get("fileName");
            String fileSize = Long.toString(((Number)item.get("fileSize")).longValue());
            String fileDate = (String) item.get("fileDate");
            //boolean file = true; // (boolean) item.get("file"); // if an item doesn't contains 'file', it occurs NullPointException.

            if(fabName!=null && machineName!=null && categoryCode!=null && categoryName!=null && fileName!=null &&
                    fileSize!=null && fileDate!=null) {

                //if(file) {
                try {
                    addDownloadItem(map, fabName, machineName, categoryCode, categoryName, fileName, fileSize, fileDate);
                } catch (ParseException e) {
                    log.error("create download form error (date="+fileDate+")");
                    error.setReason(RSSErrorReason.INVALID_PARAMETER);
                    resBody.put("error", error.getRSSError());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
                /*} else {
                    try {
                        if(!fileDownloader.createFtpDownloadFileList(requestList, fabName, machineName, categoryCode,
                                categoryName, null, null, fileName)) {
                            log.error("failed to create file-list");
                            error.setReason(RSSErrorReason.INTERNAL_ERROR);
                            resBody.put("error", error.getRSSError());
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);
                        }
                    } catch (InterruptedException e) {
                        log.error("stopped creating download list");
                        error.setReason(RSSErrorReason.INTERNAL_ERROR);
                        resBody.put("error", error.getRSSError());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);
                    }
                }*/
            } else {
                log.error("parameter failed", LogType.control);
                error.setReason(RSSErrorReason.INVALID_PARAMETER);
                resBody.put("error", error.getRSSError());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
            }
        }

        map.forEach((m, submap)->submap.forEach((c, dlForm)->requestList.add(dlForm)));
        //log.info("requestList size="+requestList.size());
        String downloadId = fileDownloader.addRequest(JobType.manual, CollectType.ftp, requestList);
        //log.info("downloadId: " + downloadId);
        resBody.put("downloadId", downloadId);
        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    @DeleteMapping("/download/{downloadId}")
    @ResponseBody
    public ResponseEntity<?> ftpDownloadCancel(HttpServletRequest request, @PathVariable("downloadId") String downloadId) {
        log.info(String.format("[Delete] %s", request.getServletPath()), LogType.control);
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        if (downloadId == null) {
            log.warn("downloadId is null", LogType.control);
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        if (!fileDownloader.cancelRequest(downloadId)) {
            log.warn("downloadId is invalid", LogType.control);
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping("/download/{downloadId}")
    @ResponseBody
    public ResponseEntity<?> ftpDownloadStatus(HttpServletRequest request, @PathVariable("downloadId") String downloadId) {
        log.info(String.format("[Get] %s", request.getServletPath()), LogType.control);
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        if(downloadId == null) {
            log.warn("downloadId is null", LogType.control);
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        log.trace("ftpDownloadStatus(downloadId="+downloadId+")", LogType.control);

        if(fileDownloader.isValidId(downloadId)==false) {
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        return ResponseEntity.status(HttpStatus.OK).body(new DownloadStatusResponseBody(fileDownloader, downloadId));
    }

    @GetMapping("/validation/{downloadId}")
    @ResponseBody
    public ResponseEntity<?> isFtpDownloadFile(
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

    @GetMapping("/storage/{downloadId}")
    @ResponseBody
    public ResponseEntity<?> ftpDownloadFile(
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
            log.info("case 1");
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

        DownloadHistoryVo history = new DownloadHistoryVo();
        history.setDl_user(decodedAccess.getUserName());
        history.setDl_type("1"); //RSS_TYPE_FTP_MANUAL = 1;

        if(downloadId == null) {
            log.error("invalid param", LogType.control);
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            history.setDl_status("Download Failed (File not found)");
            dlHistory.addDlHistory(history);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        if(fileDownloader.isValidId(downloadId)==false) {
            log.error("invalid dlId", LogType.control);
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            history.setDl_status("Download Failed (File not found)");
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
            String dlFilename = createZipFilename(downloadId, decodedAccess.getUserName());
            HttpHeaders headers = new HttpHeaders();
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

    @PostMapping("/search")
    @ResponseBody
    public ResponseEntity postFileSearch(HttpServletRequest request, @RequestBody Map<String, Object> param) {

        log.info("POST "+request.getServletPath(), LogType.control);
        Map<String, Object> responseBody = new HashMap<>();

        if(param==null) {
            log.error("no param", LogType.control);
            RSSError error = new RSSError();
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            responseBody.put("error", error);
            return new ResponseEntity(responseBody, HttpStatus.BAD_REQUEST);
        }

        try {
            List<String> fabNames = getObjectFromMap(param, "fabNames", new ArrayList<>());
            List<String> machineNames = getObjectFromMap(param, "machineNames", new ArrayList<>());
            List<String> categoryCodes = getObjectFromMap(param, "categoryCodes", new ArrayList<>());
            List<String> categoryNames = getObjectFromMap(param, "categoryNames", new ArrayList<>());
            String startDate = getObjectFromMap(param, "startDate", new String());
            if(startDate.equals("")) {
                startDate = "1970101000000";
            }
            String endDate = getObjectFromMap(param, "endDate", new String());
            if(endDate.equals("")) {
                endDate = "99991231235959";
            }

            int depth = 999;
            if(param.containsKey("depth")) {
                depth = ((Number)param.get("depth")).intValue();
            }

            if((fabNames.size() != machineNames.size()) || (categoryCodes.size() != categoryNames.size())) {
                throw new RuntimeException("bad parameter");
            }

            String searchId = fileSearchManager.requestFtpSearch(fabNames.toArray(new String[0]), machineNames.toArray(new String[0]),
                    categoryNames.toArray(new String[0]), categoryCodes.toArray(new String[0]), startDate, endDate, depth);

            responseBody.put("searchId", searchId);
            return new ResponseEntity(responseBody, HttpStatus.OK);

        } catch (RuntimeException|FileSearchException e) {
            log.error("failed to get file list. "+e.getMessage(), LogType.control, LogType.exception);
            RSSError error = new RSSError();
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            responseBody.put("error", error);
            return new ResponseEntity(responseBody, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/search/{searchId}")
    public ResponseEntity getFileSearch(HttpServletRequest request, @PathVariable String searchId) {

        log.info("GET "+request.getServletPath(), LogType.control);
        Map<String, Object> responseBody = new HashMap<>();

        FileSearchInfo info = fileSearchManager.getSearchInfo(searchId);
        if(info==null) {
            log.error("failed to get search info. "+searchId, LogType.control);
            RSSError error = new RSSError();
            error.setReason(RSSErrorReason.NOT_FOUND);
            responseBody.put("error", error);
            return new ResponseEntity(responseBody, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(info, HttpStatus.OK);
    }

    @DeleteMapping("/search/{searchId}")
    public ResponseEntity deleteFileSearch(HttpServletRequest request, @PathVariable String searchId) {

        log.info("DELETE "+request.getServletPath(), LogType.control);
        fileSearchManager.cancelJob(searchId);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/search/result/{searchId}")
    public ResponseEntity getFileSearchResult(HttpServletRequest request, @PathVariable String searchId) {

        log.info("GET "+request.getServletPath(), LogType.control);
        Map<String, Object> responseBody = new HashMap<>();

        List<RSSFtpSearchResponse> files = fileSearchManager.getSearchedFileList(searchId);

        if(files==null) {
            log.error("failed to get searched file list. "+searchId, LogType.control);
            RSSError error = new RSSError();
            error.setReason(RSSErrorReason.NOT_FOUND);
            responseBody.put("error", error);
            return new ResponseEntity(responseBody, HttpStatus.NOT_FOUND);
        }

        responseBody.put("lists", files);
        return new ResponseEntity(responseBody, HttpStatus.OK);
    }

    private void addDownloadItem(final Map map, String fab, String tool, String logType, String logTypeStr,
                                 String file, String size, String date) throws ParseException {

        FtpDownloadRequestForm form;

        /*
        2020-11-26 Ted
        As decompression process has been removed on the specification of directory structurizaion,
        We have to request the same category's files at once, not separated.

        // We have to consider sub-directories now.
        String[] paths = file.split("/");
        if(paths.length!=1) {
            // This file places at the sub-directory.
            // In this case, a key composes with 'logType/sub-directory-name' pattern.
            logType = logType+"/"+paths[0];
        }
        */

        if(map.containsKey(tool)) {
            Map<String, FtpDownloadRequestForm> submap = (Map<String, FtpDownloadRequestForm>) map.get(tool);
            if(submap.containsKey(logType)) {
                form = submap.get(logType);
            } else {
                form = new FtpDownloadRequestForm(fab, tool, logType, logTypeStr);
                submap.put(logType, form);
            }
        } else {
            form = new FtpDownloadRequestForm(fab, tool, logType, logTypeStr);
            Map<String, FtpDownloadRequestForm> submap = new HashMap<>();
            submap.put(logType, form);
            map.put(tool, submap);
        }

        if(form==null) {
            log.error("fatal: addDownloadItem could not find form", LogType.control);
            return;
        }
        form.addFile(file, Long.parseLong(size), date);
    }
}
