package jp.co.canon.rss.logmanager.manager;


import com.google.gson.Gson;
import jp.co.canon.rss.logmanager.exception.StatusResourceNotFoundException;
import jp.co.canon.rss.logmanager.repository.RemoteJobRepository;
import jp.co.canon.rss.logmanager.system.ClientManageService;
import jp.co.canon.rss.logmanager.vo.RemoteJobVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Getter
public class NotiStatus extends Thread {
    protected final String  jobId;
    protected boolean       dead;
    protected AtomicLong works;
    protected AtomicLong finishes;
    protected ClientManageService client;
    protected SendMail sendMail;

    private RemoteJobRepository remoteJobRepository;
    private String downloadPath;
    private int runId;
    private String company;
    private String fab;
    private String prefix;
    private String [] crasTo;
    private String crasTitle;
    private String [] errTo;
    private String errTitle;
    private String errContents;
    private String [] verTo;
    private String verTitle;
    private String requstId;

    private List<SendingTime> sendingTimes;
    private int nowDay;
    private boolean crasEnable;
    private boolean errorSummaryEnable;
    private boolean versionEnable;
    private int crasBefore;
    private int errBefore;
    private int verBefore;
    private boolean oldStop = false;
    private String rid = null;

    public NotiStatus(
            String jobId,
            NotiManager manager
    ) {

        this.jobId = jobId;
        this.dead = false;
        this.remoteJobRepository = manager.getRemoteJobRepository();
        this.downloadPath = manager.getDownloadPath();
        this.sendingTimes = new ArrayList<SendingTime>();

        works = new AtomicLong(0);
        finishes = new AtomicLong(0);
        client = new ClientManageService();
    }

    @Override
    public void run() {
        initialize();

        watch();

        shutdown();
    }


    private void initialize() {
        String[] jobinfo = this.jobId.split("_");
        this.runId = Integer.parseInt(jobinfo[1]);

        if((this.remoteJobRepository != null) && (this.runId != 0)) {
            RemoteJobVo resultRemoteJobDetail = remoteJobRepository.findByJobId(this.runId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            this.crasEnable = resultRemoteJobDetail.getNotification().getCrasEnable();
            this.errorSummaryEnable = resultRemoteJobDetail.getNotification().getErrorSummaryEnable();
            this.versionEnable = resultRemoteJobDetail.getNotification().getVersionEnable();

            if((this.crasEnable == false) && (this.errorSummaryEnable == false) && (this.versionEnable == false)) {
                return;
            }

            if(this.crasEnable == true){
                this.crasTo = resultRemoteJobDetail.getNotification().getMailContextVoCras().getCustomEmails();
                this.crasTitle = resultRemoteJobDetail.getNotification().getMailContextVoCras().getSubject();
                this.crasBefore = resultRemoteJobDetail.getNotification().getMailContextVoCras().getBefore();
            }
            if(this.errorSummaryEnable == true) {
                this.errTo = resultRemoteJobDetail.getNotification().getMailContextVoError().getCustomEmails();
                this.errTitle = resultRemoteJobDetail.getNotification().getMailContextVoError().getSubject();
                this.errContents = resultRemoteJobDetail.getNotification().getMailContextVoError().getBody();
                this.errBefore = resultRemoteJobDetail.getNotification().getMailContextVoError().getBefore();
            }
            if(this.versionEnable == true) {
                this.verTo = resultRemoteJobDetail.getNotification().getMailContextVoVersion().getCustomEmails();
                this.verTitle = resultRemoteJobDetail.getNotification().getMailContextVoVersion().getSubject();
                this.verBefore = resultRemoteJobDetail.getNotification().getMailContextVoVersion().getBefore();
            }

            this.prefix = String.format("http://%s:%d", resultRemoteJobDetail.getSiteVoList().getCrasAddress(), resultRemoteJobDetail.getSiteVoList().getCrasPort());
            this.company = resultRemoteJobDetail.getSiteVoList().getCrasCompanyName();
            this.fab =  resultRemoteJobDetail.getSiteVoList().getCrasFabName();
            String[] times = resultRemoteJobDetail.getNotification().getSendingTime();
            this.sendingTimes.clear();
            for(int idx=0; idx<times.length ; idx++) {
                LocalTime lt = LocalTime.parse(times[idx], DateTimeFormatter.ofPattern("HH:mm"));
                SendingTime time = new SendingTime();
                time.setSec(lt.toSecondOfDay());
                time.setExcuted(false);
                this.sendingTimes.add(time);
            }

            this.nowDay = Integer.parseInt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd")));


            this.sendMail = new SendMail(resultRemoteJobDetail.getSiteVoList().getEmailUserName(),
                                        resultRemoteJobDetail.getSiteVoList().getEmailPassword(),
                                        resultRemoteJobDetail.getSiteVoList().getEmailAddress(),
                                        resultRemoteJobDetail.getSiteVoList().getEmailPort(),
                                        resultRemoteJobDetail.getSiteVoList().getEmailFrom());
        }
    }

    private boolean isStop() throws StatusTerminateException{
        boolean ret = true;

        try {
            if (this.runId == 0) {
                log.info("runId is zero");
                return ret;
            }

            if (this.remoteJobRepository != null) {
                RemoteJobVo resultRemoteJobDetail = remoteJobRepository.findByJobId(this.runId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                ret = (boolean) resultRemoteJobDetail.isStop();
                if(ret != oldStop) {
                    oldStop = ret;
                    initialize();
                }
            } else {
                log.info("JobRepository is null");
            }
        } catch ( RuntimeException e) {
            throw new StatusTerminateException("Noti Thread is Terminated");
        }

        return ret;
    }

    private String getAttiributeFromJson(String json, String attribute){
        JSONParser parser = new JSONParser();
        JSONObject obj = null;

        try {
            obj = (JSONObject)parser.parse(json);
        } catch (ParseException e) {
            log.error("json ParseException");
            e.printStackTrace();
        }

        return (String)obj.get(attribute);
    }

    private StatusDetail notiCrasRun() {
        StatusDetail status = new StatusDetail();
        status.setStatus(status.STATUS_IDLE, status.DETAIL_NONE);
        String url = this.prefix + ReqURL.API_CRAS_JOB;
        String msg = null;
        Gson gson = new Gson();
        ReqNotiRun req = new ReqNotiRun();

        req.setCompany(this.company);
        req.setFab(this.fab);
        req.setBefore(this.verBefore);
        msg = gson.toJson(req);

        this.requstId = null;

        HttpResponse res = client.post(url, msg);
        if (res.getStatusLine().getStatusCode() == 200) {
            ResponseHandler<String> handler = new BasicResponseHandler();
            try {
                String body = handler.handleResponse(res);
                if (body != null) {
                    this.requstId = getAttiributeFromJson(body, "rid");
                    if(this.requstId == null) {
                        status.setStatus(status.STATUS_ERROR, "Request ID is null");
                    }
                } else {
                    status.setStatus(status.STATUS_ERROR, "Response Body is null");
                }
            } catch (Exception e) {
                status.setStatus(status.STATUS_ERROR, e.toString());
                log.error(e.toString());
            }
        } else if (res.getStatusLine().getStatusCode() == 204) {
            status.setStatus(status.STATUS_IDLE, res.getStatusLine().getReasonPhrase());
            log.info(res.getStatusLine().getReasonPhrase());
        } else {
            status.setStatus(status.STATUS_ERROR, res.getStatusLine().getReasonPhrase());
            log.error(res.getStatusLine().getReasonPhrase());
        }

        return status;
    }

    private StatusDetail notiCrasStatus() {
        StatusDetail status = new StatusDetail();
        status.setStatus(status.STATUS_IDLE, status.DETAIL_NONE);
        String url = this.prefix + ReqURL.API_CRAS_JOB + "/" +this.requstId;
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String[] download = {"download_data", "download_judge", "download_mail", "download_pdf"};
        String[] file = {"CRASDATA", "JudgeResult", "Contents", "CRAS_Report"};
        String[] fileName = new String[download.length];
        String[] downloadPath = new String[download.length];
        String[] to = this.crasTo;
        String title = this.crasTitle;
        String contents = null;

        HttpResponse res = client.get(url);
        if (res.getStatusLine().getStatusCode() == 200) {
            ResponseHandler<String> handler = new BasicResponseHandler();
            try {
                String body = handler.handleResponse(res);
                if(body != null){
                    status.setStatus(getAttiributeFromJson(body, "status"));
                    if(status.getStatus().equals(status.STATUS_ERROR)){
                        return status;
                    }

                    for(int idx = 0; idx < download.length; idx++){
                        downloadPath[idx] = getAttiributeFromJson(body, download[idx]);
                        fileName[idx] = "/" + file[idx] + "_" + this.company + "-" + this.fab + "_" +  date + ".zip";
                    }

                    status = donwloadAttachMulitFile(downloadPath,fileName, to, title, contents,"CRAS");
                } else {
                    status.setStatus(status.STATUS_ERROR, "response body is null");
                }
            } catch (Exception e) {
                status.setStatus(status.STATUS_ERROR, e.toString());
                log.error(e.toString());
            }
        }
        return status;
    }

    private StatusDetail notiErrorSummaryRun() {
        StatusDetail status = new StatusDetail();
        status.setStatus(status.STATUS_IDLE, status.DETAIL_NONE);

        String url = this.prefix + ReqURL.API_SUMMARY_JOB;
        String msg = null;
        Gson gson = new Gson();
        ReqNotiRun req = new ReqNotiRun();

        req.setCompany(this.company);
        req.setFab(this.fab);
        req.setBefore(this.verBefore);
        msg = gson.toJson(req);

        HttpResponse res = client.post(url, msg);
        if (res.getStatusLine().getStatusCode() == 200) {
            ResponseHandler<String> handler = new BasicResponseHandler();
            try {
                String body = handler.handleResponse(res);
                if (body != null) {
                    this.requstId = getAttiributeFromJson(body, "rid");
                } else {
                    status.setStatus(status.STATUS_ERROR, "response body is null");
                }
            } catch (Exception e) {
                status.setStatus(status.STATUS_ERROR, e.toString());
            }
        } else if (res.getStatusLine().getStatusCode() == 204) {
            status.setStatus(status.STATUS_IDLE, res.getStatusLine().getReasonPhrase());
            log.info(res.getStatusLine().getReasonPhrase());
        } else {
            status.setStatus(status.STATUS_ERROR, res.getStatusLine().getReasonPhrase());
        }

        return status;
    }

    private StatusDetail notiErrorSummaryStatus() {
        StatusDetail status = new StatusDetail();
        status.setStatus(status.STATUS_IDLE, status.DETAIL_NONE);
        String url = this.prefix + ReqURL.API_SUMMARY_JOB + "/" + this.requstId;
        String download = null;
        String fileName = "/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) + "_" + this.company + "-" + this.fab + "_" + "error-summary.zip";
        String[] to = this.errTo;
        String title = this.errTitle;
        String contents = this.errContents;

        HttpResponse res = client.get(url);
        if (res.getStatusLine().getStatusCode() == 200) {
            ResponseHandler<String> handler = new BasicResponseHandler();
            try {
                String body = handler.handleResponse(res);
                if(body != null){
                    status.setStatus(getAttiributeFromJson(body, "status"));
                    download = getAttiributeFromJson(body, "download");
                    if(status.getStatus().equals(status.STATUS_SUCCESS) && download != null){
                        status = donwloadAttachFile(download,fileName, to, title, contents,"ERROR");
                    } else {
                        status.setStatus(status.STATUS_ERROR, "response body is not invaild : donwload "+download);
                    }
                } else {
                    status.setStatus(status.STATUS_ERROR, "response body is null");
                }
            } catch (Exception e) {
                status.setStatus(status.STATUS_ERROR, e.toString());
            }
        }
        return status;
    }

    private StatusDetail notiVersionRun() {
        StatusDetail status = new StatusDetail();
        status.setStatus(status.STATUS_IDLE, status.DETAIL_NONE);
        String url = this.prefix + ReqURL.API_VERSION_JOB;
        String msg = null;
        Gson gson = new Gson();
        ReqNotiRun req = new ReqNotiRun();

        req.setCompany(this.company);
        req.setFab(this.fab);
        req.setBefore(this.verBefore);
        msg = gson.toJson(req);

        HttpResponse res = client.post(url, msg);
        if (res.getStatusLine().getStatusCode() == 200) {
            ResponseHandler<String> handler = new BasicResponseHandler();
            try {
                String body = handler.handleResponse(res);
                if (body != null) {
                    this.requstId = getAttiributeFromJson(body, "rid");
                } else {
                    this.requstId = null;
                    status.setStatus(status.STATUS_ERROR, "response body is null");
                }
            } catch (Exception e) {
                status.setStatus(status.STATUS_ERROR, e.toString());
            }
        } else if (res.getStatusLine().getStatusCode() == 204) {
            status.setStatus(status.STATUS_IDLE, res.getStatusLine().getReasonPhrase());
            log.info(res.getStatusLine().getReasonPhrase());
        } else {
            status.setStatus(status.STATUS_ERROR, res.getStatusLine().getReasonPhrase());
        }

        return status;
    }

    private StatusDetail notiVersionStatus() {
        StatusDetail status = new StatusDetail();
        status.setStatus(status.STATUS_IDLE, status.DETAIL_NONE);
        String url = this.prefix + ReqURL.API_VERSION_JOB + "/" + this.requstId;
        String download = null;
        String fileName = "/" + "VERSION" + "_" + this.company + "-" + this.fab + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String[] to = this.verTo;
        String title = this.verTitle;
        String contents = null;

        HttpResponse res = client.get(url);
        if (res.getStatusLine().getStatusCode() == 200) {
            ResponseHandler<String> handler = new BasicResponseHandler();
            try {
                String body = handler.handleResponse(res);
                if(body != null){
                    status.setStatus(getAttiributeFromJson(body, "status"));
                    download = getAttiributeFromJson(body, "download");
                    if(status.getStatus().equals(status.STATUS_SUCCESS) && download != null){
                        status = donwloadAttachFile(download,fileName, to, title, contents,"VERSION");
                    } else {
                        status.setStatus(status.STATUS_ERROR, "response body is not invaild : "+download);
                    }
                } else {
                    status.setStatus(status.STATUS_ERROR, "response body is null");
                }
            } catch (Exception e) {
                status.setStatus(status.STATUS_ERROR, e.toString());
            }
        }
        return status;
    }

    private void notiCras() throws StatusTerminateException {
        StatusDetail status = new StatusDetail();
        status.setStatus(status.STATUS_IDLE);
        status.setDetail(status.DETAIL_NONE);
        this.requstId = null;

       try {
           while (isStop() != true) {
               if (this.requstId == null) {
                   sleep(10000);
                   status = notiCrasRun();
               } else {
                   sleep(10000);
                   status = notiCrasStatus();
               }

               statusUpdate(this.runId, "crasDataStatus", status);
               if ((status.getStatus().equals(status.STATUS_ERROR)) || (status.getStatus().equals(status.STATUS_SUCCESS)) || (status.getStatus().equals(status.STATUS_CANCEL))) {
                   log.info("cras job(" + this.jobId + ") is completed... status(" + status.getStatus() + ")");
                   return;
               }
           }
        } catch (StatusResourceNotFoundException e) {
           throw new StatusTerminateException("noti thread is terminated");
        } catch (InterruptedException e) {
            log.error("interrupted exception : " + e);
        } catch (RuntimeException e) {
            log.error("exception : "+e);
        }
    }

    private void notiErrorSummary() throws StatusTerminateException {
        StatusDetail status = new StatusDetail();
        status.setStatus(status.STATUS_IDLE, status.DETAIL_NONE);
        this.requstId = null;

        try {
            while (isStop() != true) {
                if (this.requstId == null) {
                    sleep(10000);
                    status = notiErrorSummaryRun();
                } else {
                    sleep(10000);
                    status = notiErrorSummaryStatus();
                }

                statusUpdate(this.runId, "errorSummaryStatus", status);
                if ((status.getStatus().equals(status.STATUS_ERROR)) || (status.getStatus().equals(status.STATUS_SUCCESS)) || (status.getStatus().equals(status.STATUS_CANCEL))) {
                    log.info("e-summary job(" + this.jobId + ") is completed... status(" + status.getStatus() + ")");
                    return;
                }
            }
        } catch (StatusResourceNotFoundException e) {
            throw new StatusTerminateException("noti thread is terminated");
        } catch (InterruptedException e) {
            log.error("interrupted exception : " + e);
        } catch (RuntimeException e) {
            log.error("exception : "+e);
        }
    }

    private void notiVersion() throws StatusTerminateException {
        StatusDetail status = new StatusDetail();
        status.setStatus(status.STATUS_IDLE, status.DETAIL_NONE);
        this.requstId = null;

        try {
            while (isStop() == false) {

                if (this.requstId == null) {
                    sleep(10000);
                    status = notiVersionRun();
                } else {
                    sleep(10000);
                    status = notiVersionStatus();
                }

                statusUpdate(this.runId, "mpaVersionStatus", status);
                if ((status.getStatus().equals(status.STATUS_ERROR)) || (status.getStatus().equals(status.STATUS_SUCCESS)) || (status.getStatus().equals(status.STATUS_CANCEL))) {
                    log.info("version job(" + this.jobId + ") is completed... status(" + status.getStatus() + ")");
                    return;
                }
            }
        } catch (StatusResourceNotFoundException e) {
            throw new StatusTerminateException("noti thread is Terminated");
        } catch (InterruptedException e) {
            log.error("interrupted exception : " + e);
        } catch (RuntimeException e) {
            log.error("exception : "+e);
        }
    }

    private StatusDetail donwloadAttachFile(String url, String fileName, String[] to, String title, String contents, String type){
        StatusDetail status = new StatusDetail();
        status.setStatus(status.STATUS_SUCCESS, status.DETAIL_NONE);
        String savePath = this.downloadPath + fileName;
        String connectPath = this.prefix + url;

        if((this.downloadPath == null) || (this.prefix == null)) {
            status.setStatus(status.STATUS_ERROR, "downloadPath("+this.downloadPath+") or prefix("+this.prefix+") is null");
            return status;
        }

        client.download(connectPath, savePath);

        if(type.equals("VERSION")) {
            File file = new File(savePath);
            try {
                contents = FileUtils.readFileToString(file, "UTF-8");
                savePath = null;
            } catch (IOException e) {
                log.error("io exception :"+e);
                status.setStatus(status.STATUS_ERROR, e.toString());
                return status;
            }
        }

        try {
            status = sendMail.emailSend(to, title, contents, type, savePath);
        } catch (MessagingException e) {
            log.error("messaging exception :"+e);
            status.setStatus(status.STATUS_ERROR, e.toString());
        }

        return status;
    }

    private StatusDetail donwloadAttachMulitFile(String[] url, String[] fileName, String[] to, String title, String contents, String type){
        StatusDetail status = new StatusDetail();
        status.setStatus(status.STATUS_SUCCESS, status.DETAIL_NONE);
        String connectPath[] = new String[url.length-1];
        String savePath[] = new String[fileName.length-1];
        String connectTempPath = null;
        String saveTempPath = null;
        int pathIndex = 0;

        for(int idx = 0; idx < url.length; idx++){
            saveTempPath = this.downloadPath + fileName[idx];
            connectTempPath = this.prefix + url[idx];

            client.download(connectTempPath, saveTempPath);
            if(saveTempPath.contains("Contents")) {
                File file = new File(saveTempPath);
                try {
                    contents = FileUtils.readFileToString(file, "UTF-8");
                    continue;
                } catch (IOException e) {
                    log.error("io exception :"+e);
                    status.setStatus(status.STATUS_ERROR, e.toString());
                    return status;
                }
            }

            savePath[pathIndex] = saveTempPath;
            connectPath[pathIndex] = connectTempPath;
            pathIndex++;
        }

        try {
            status = sendMail.emailMultiSend(to, title, contents, type, savePath);
        } catch (MessagingException e) {
            log.info("messaging exception :"+e);
            status.setStatus(status.STATUS_ERROR, e.toString());
        }

        return status;
    }

    private void statusUpdate(int id, String column, StatusDetail status)
            throws StatusResourceNotFoundException {
        if(this.runId == 0){
            log.info("runid is zero");
            return;
        }

        RemoteJobVo remoteJobInfo = remoteJobRepository.findById(id)
                .orElseThrow(() -> new StatusResourceNotFoundException("Job not found for this id :: " + id));

        switch (column) {
            case "errorSummaryStatus":
                if(remoteJobInfo.getErrorSummaryStatus().equals(status.getStatus()) == false) {
                    remoteJobInfo.setErrorSummaryStatus(status.getStatus());
                }
                break;
            case "crasDataStatus":
                if(remoteJobInfo.getCrasDataStatus().equals(status.getStatus()) == false) {
                    remoteJobInfo.setCrasDataStatus(status.getStatus());
                }
                break;
            case "mpaVersionStatus":
                if(remoteJobInfo.getMpaVersionStatus().equals(status.getStatus()) == false) {
                    remoteJobInfo.setMpaVersionStatus(status.getStatus());
                }
                break;
        }

        remoteJobRepository.save(remoteJobInfo);
    }

    private void initializeSendingTimes() {
        int dfDay = Integer.parseInt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd")));

        if(nowDay != dfDay) {
            for(int idx=0; idx<this.sendingTimes.size() ; idx++) {
                this.sendingTimes.get(idx).setExcuted(false);
                nowDay = dfDay;
            }
        }
    }
    private int localTimeToSeconds() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime lt = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
        return lt.toSecondOfDay();
    }

    private void watch() {

        if((this.crasEnable == false) && (this.errorSummaryEnable == false) && (this.versionEnable == false)) {
            return;
        }
        try {
            while (true) {
                if (isStop() != true) {
                    initializeSendingTimes();
                    int nowSec = localTimeToSeconds();

                    for (int idx = 0; idx < this.sendingTimes.size(); idx++) {
                        int dfSec = this.sendingTimes.get(idx).getSec();
                        boolean isExcuted = this.sendingTimes.get(idx).isExcuted();

                        if ((nowSec >= dfSec) && (isExcuted == false)) {
                            this.sendingTimes.get(idx).setExcuted(true);

                            if (this.errorSummaryEnable == true) {
                                notiErrorSummary();
                            }
                            if (this.crasEnable == true) {
                                notiCras();
                            }
                            if (this.versionEnable == true) {
                                notiVersion();
                            }
                        }
                    }
                    Thread.sleep(10000);
                } else {
                    Thread.sleep(1000);
                }
            }
        } catch (StatusTerminateException e) {
            log.info("status terminate exception : " + e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shutdown() {
        dead = true;
    }

    @Setter
    @Accessors(chain = true, fluent = true)
    public static class Builder {
        private NotiManager manager;
        private String      jobType;

        public NotiStatus build(long key) {

            String id = createJobId(jobType, key);

            if(jobType.equals(NotiManager.REMOTE_TYPE)) {
                return new RemoteNotiStatus(id, manager);
            }
            return null;
        }

        public String createJobId(String type, long key) {
            final String format = "%snoit_%05d";

            return String.format(format, type, key);
        }
    }
}
