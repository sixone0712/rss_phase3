package jp.co.canon.cks.eec.fs.rssportal.controller;

import jp.co.canon.cks.eec.fs.rssportal.Defines.RSSErrorReason;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import jp.co.canon.cks.eec.fs.rssportal.common.LogType;
import jp.co.canon.cks.eec.fs.rssportal.common.Tool;
import jp.co.canon.cks.eec.fs.rssportal.model.error.RSSError;
import jp.co.canon.cks.eec.fs.rssportal.model.auth.AccessToken;
import jp.co.canon.cks.eec.fs.rssportal.model.histories.RSSHistoryList;
import jp.co.canon.cks.eec.fs.rssportal.service.DownloadHistoryService;
import jp.co.canon.cks.eec.fs.rssportal.service.JwtService;
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
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/histories")
public class DlHistoryController {
    private final String HISTORY_RESULT = "result";
    private final String HISTORY_DATA = "data";
    private final DownloadHistoryService serviceDlHistory;
    private final JwtService jwtService;
    private final EspLog log = new EspLog(getClass());

    @Value("${rssportal.history.cacheBase}")
    private String cacheBase;

    @Autowired
    public DlHistoryController(DownloadHistoryService serviceDlHistory, JwtService jwtService) {
        this.serviceDlHistory = serviceDlHistory;
        this.jwtService = jwtService;
    }

    @GetMapping("/downloads")
    @ResponseBody
    public ResponseEntity<?> getHistoryList(HttpServletRequest request) throws Exception {
        log.info(String.format("[Get] %s", request.getServletPath()));
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        List<DownloadHistoryVo> lists = serviceDlHistory.getHistoryList();
        List<RSSHistoryList> newList = new ArrayList<RSSHistoryList>();

        for(DownloadHistoryVo list : lists) {
            RSSHistoryList history = new RSSHistoryList();
            SimpleDateFormat conTimeFormat  = new SimpleDateFormat("yyyyMMddHHmmss");
            history.setHistoryId(list.getId());
            history.setType(list.getDl_type());
            history.setDate(list.getDl_date() != null ? conTimeFormat.format(list.getDl_date()) : null);
            history.setFileName(list.getDl_filename());
            history.setUserName(list.getDl_user());
            history.setStatus(list.getDl_status());
            history.setHistoryId(list.getId());
            history.setHistoryId(list.getId());
            newList.add(history);
        }

        resBody.put("lists", newList);
        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    @PostMapping("/downloads")
    @ResponseBody
    public ResponseEntity<?> addDlHistory(HttpServletRequest request, @RequestBody Map<String, Object> param)  throws Exception {
        log.info(String.format("[Get] %s", request.getServletPath()));
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();
        String accessToken = request.getHeader("Authorization");

        if(param == null || param.size() == 0 || accessToken == null || accessToken.isEmpty()) {
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        String type = param.containsKey("type") ? param.get("type").toString() : null;
        String filename = param.containsKey("filename") ? (String)param.get("filename") : null;
        String status = param.containsKey("status") ? (String)param.get("status") : null;

        if(type == null || type.isEmpty() || filename == null || filename.isEmpty() || status == null || status.isEmpty()) {
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
        }

        try {
            DownloadHistoryVo dlVo = new DownloadHistoryVo();
            AccessToken decodedAccess = jwtService.decodeAccessToken(accessToken);
            dlVo.setDl_user(decodedAccess.getUserName());
            dlVo.setDl_type(type);
            dlVo.setDl_filename(filename);
            dlVo.setDl_status(status);
            if(!serviceDlHistory.addDlHistory(dlVo)) {
                throw new Exception();
            }
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (Exception e) {
            error.setReason(RSSErrorReason.INTERNAL_ERROR);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);
        }
    }

    @GetMapping("/total")
    @ResponseBody
    public ResponseEntity<?> getTotalCnt(HttpServletRequest request) throws Exception {
        log.info(String.format("[Get] %s", request.getServletPath()));
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        int cnt = serviceDlHistory.getHistoryTotalCnt();

        if(cnt<0){
            resBody.put("error", error.getRSSError());
        }
        else
        {
            resBody.put("cnt", cnt);
        }
        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    @GetMapping("/export")
    @ResponseBody
    public ResponseEntity exportDownloadHistory(HttpServletRequest request, HttpServletResponse response) {
        log.info(String.format("[Get] %s", request.getServletPath()), LogType.control);

        File rootFile = Paths.get(cacheBase).toFile();
        if(!rootFile.exists()) {
            log.info("create the cache directory", LogType.control);
            rootFile.mkdirs();
        }

        File file;
        int retry = 0;
        do {
            String fileName = String.format("%s_%d.csv", jwtService.getCurAccTokenUserName(), System.currentTimeMillis());
            file = Paths.get(cacheBase, fileName).toFile();
            if(file!=null && !file.exists()) {
                break;
            }
            if(++retry>3) {
                log.error("failed to create tmp file", LogType.control);
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } while(true);

        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
            List<DownloadHistoryVo> list = serviceDlHistory.getHistoryList();
            if(list==null || list.size()==0) {
                writer.write("no history");
                writer.flush();
            } else {
                SimpleDateFormat dateFormat = Tool.getSimpleDateFormat();
                for(DownloadHistoryVo history: list) {
                    StringBuilder line = new StringBuilder();
                    line.append(history.getDl_user()).append(",");
                    line.append(dateFormat.format(history.getDl_date())).append(",");
                    line.append(history.getDl_filename()).append(",");
                    line.append(getDownloadHistoryTypeString(history.getDl_type())).append(",");
                    line.append(history.getDl_status()).append("\r\n");
                    writer.write(line.toString());
                    writer.flush();
                }
            }
            writer.close();
        } catch (FileNotFoundException e) {
            log.error("failed to create file "+e.getMessage(), LogType.control);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            log.error("failed to create file "+e.getMessage(), LogType.control);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            InputStream is = new FileInputStream(file);
            InputStreamResource isr = new InputStreamResource(is);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentLength(file.length());
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            response.setHeader("Content-Disposition", "attachment; filename="+"download-history.csv");
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(isr);
        } catch (FileNotFoundException e) {
            log.error("failed to open stream "+e.getMessage(), LogType.control, LogType.exception);
        }
        return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getDownloadHistoryTypeString(String number) {
        if(number==null) {
            return "unknown";
        }
        switch (number) {
            case "1": return "Manual download(FTP)";
            case "2": return "Auto download(FTP)";
            case "3": return "Manual download(VFTP/SSS)";
            case "4": return "Manual download(VFTP/COMPAT)";
            case "5": return "Auto download(VFTP/COMPAT)";
            case "6": return "Auto download(VFTP/SSS)";
            default:  return "unknown";
        }
    }

}
