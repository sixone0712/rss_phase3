package jp.co.canon.cks.eec.fs.rssportal.background;

import jp.co.canon.ckbs.eec.fs.collect.FileServiceCollectConnectorFactory;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnector;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnectorFactory;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.ConfigurationService;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import jp.co.canon.cks.eec.fs.rssportal.common.LogType;
import jp.co.canon.cks.eec.fs.rssportal.model.DownloadStatusResponseBody;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
public class FileDownloader extends Thread {

    /* Downloader status */
    private static final String STS_INVALID_ID = "invalid-id";
    private static final String STS_IN_PROGRESS = "in-progress";
    private static final String STS_ERROR = "error";
    private static final String STS_DONE = "done";
    private static final String STS_IN_COMPRESS = "in-compress";

    private static final String RESULT_FILE_NAME = "result.log";
    private static final String __download_id__ = "downloadId";
    private static final String __status__ = "status";
    private static final String __path__ = "path";
    private static final String __total_files__ = "totalFiles";
    private static final String __download_files__ = "downloadedFiles";
    private static final String __total_size__ = "totalSize";
    private static final String __download_size__ = "downloadSize";
    private static final String __collect_type__ = "collectType";
    private static final String __last_update__ = "lastUpdate";
    private static final String __base_dir__ = "baseDir";
    private static final String __download_url__ = "downloadUrl";
    private static final String __fabs__ = "fabs";

    private final FileServiceManageConnectorFactory fileServiceManageConnectorFactory;
    @Getter
    private FileServiceManageConnector fileServiceManageConnector;
    @Getter
    private final FileServiceCollectConnectorFactory fileServiceCollectConnectorFactory;
    @Getter
    private final ConfigurationService configurationService;

    private final HashMap<String, FileDownloadExecutor> executorList;
    private FileServiceManageConnector connector;

    @Value("${rssportal.collect.cacheBase}")
    private String downloadCacheDir;

    @Value("${rssportal.collect.resultBase}")
    private String downloadResultDir;

    @Value("${rssportal.file-collect-service.retry}")
    private int fileServiceRetryCount;

    @Value("${rssportal.file-collect-service.retry-interval}")
    private int fileServiceRetryInterval;

    @Value("${rssportal.file-service-manager.addr}")
    private String fileServiceAddress;

    @Value("${rssportal.downloader.max-threads}")
    @Getter
    private int maxThreads;

    @Autowired
    public FileDownloader(FileServiceManageConnectorFactory fileServiceManageConnectorFactory,
                          FileServiceCollectConnectorFactory fileServiceCollectConnectorFactory,
                          ConfigurationService configurationService) {
        log.info("initialize FileDownloader");
        this.fileServiceManageConnectorFactory = fileServiceManageConnectorFactory;
        this.fileServiceCollectConnectorFactory = fileServiceCollectConnectorFactory;
        this.configurationService = configurationService;
        executorList = new HashMap<>();
    }

    @PostConstruct
    private void _init() {
        fileServiceManageConnector = fileServiceManageConnectorFactory.getConnector(fileServiceAddress);
    }

    public String addRequest(JobType jobType, CollectType collectType, final List<DownloadRequestForm> dlList) {
        log.info("addRequest( request-size="+dlList.size()+")");

        FileDownloadExecutor executor = new FileDownloadExecutor(
                jobType.name(), collectType.name(),"", this, dlList, true);
        executor.setAttrDownloadFilesViaMultiSessions(true);
        executorList.put(executor.getId(), executor);
        executor.start();
        log.info("jobid="+executor.getId()+" has been started");
        return executor.getId();
    }

    public boolean cancelRequest(@NonNull final String downloadId) {
        if(!isValidId(downloadId)) {
            log.error("cancelRequest/ invalid downloadId "+downloadId);
            return false;
        }
        FileDownloadExecutor executor = executorList.get(downloadId);
        if(executor!=null) {
            executor.stop();
        }
        return true;
    }

    public void finishRequest(final String downloadId) {
        log.info("finishRequest downloadId="+downloadId);
        if(!isHeapManaged(downloadId)) {
            log.error("finishRequest invalid downloadId="+downloadId);
            return;
        }

        FileDownloadExecutor executor = executorList.get(downloadId);
        if(executor!=null) {
            File downloadPath = Paths.get(downloadResultDir, executor.getId()).toFile();
            if(!downloadPath.exists()) {
                log.info("create result path "+downloadPath.toString());
                downloadPath.mkdirs();
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put(__download_id__, downloadId)
                    .put(__status__, getStatus(downloadId))
                    .put(__path__, executor.getDownloadPath())
                    .put(__total_files__, executor.getTotalFiles())
                    .put(__download_files__, executor.getDownloadFiles())
                    .put(__total_size__, executor.getTotalSize())
                    .put(__download_size__, executor.getDownloadSize())
                    .put(__collect_type__, executor.getFtpType())
                    .put(__last_update__, executor.getLastUpdate())
                    .put(__base_dir__, executor.getBaseDir());

            String url = getDownloadUrl(downloadId);
            if(url!=null) {
                jsonObject.put(__download_url__, url);
            }

            JSONArray jFabs = new JSONArray(executor.getFabs());
            jsonObject.put(__fabs__, jFabs);

            File resultFile = Paths.get(downloadPath.toString(), RESULT_FILE_NAME).toFile();
            if(resultFile.exists()) {
                log.error("finishRequest result file exists");
                return;
            }
            try {
                FileOutputStream os = new FileOutputStream(resultFile);
                os.write(jsonObject.toString().getBytes());
                os.flush();
                os.close();
                executor.finish();
                executorList.remove(downloadId);
            } catch (FileNotFoundException e) {
                log.error("finishRequest failed to create result file");
            } catch (IOException e) {
                log.error("finishRequest failed to write result file");
            }
        }
    }

    public void getDownloadStatusResponseBody(DownloadStatusResponseBody response) {
        if(response==null) {
            log.error("getDownloadStatusResponseBody null input");
            return;
        }
        String downloadId = response.getDownloadId();
        if(downloadId==null || !isValidId(downloadId)) {
            log.error("getDownloadStatusResponseBody null downloadId");
            return;
        } else if(isHeapManaged(downloadId)) {
            // The object places in heap.
            FileDownloadExecutor executor = executorList.get(downloadId);
            if(executor!=null) {
                response.setStatus(getStatus(downloadId));
                response.setTotalFiles(executor.getTotalFiles());
                response.setDownloadedFiles(executor.getDownloadFiles());
                response.setTotalSize(executor.getTotalSize());
                response.setDownloadSize(executor.getDownloadSize());
                response.setDownloadUrl(getDownloadUrl(downloadId));
            }
        } else {
            // Find the object from filesystem.
            File downloadPath = Paths.get(downloadResultDir, downloadId).toFile();
            if(downloadPath.exists() && downloadPath.isDirectory()) {
                File resultFile = Paths.get(downloadPath.toString(), RESULT_FILE_NAME).toFile();
                if(resultFile.exists()) {
                    try {
                        String resultString = FileUtils.readFileToString(resultFile);
                        JSONObject json = new JSONObject(resultString);

                        response.setStatus(json.getString(__status__));
                        response.setTotalFiles((int)json.getLong(__total_files__));
                        response.setDownloadedFiles(json.getLong(__download_files__));
                        response.setTotalSize(json.getLong(__total_size__));
                        response.setDownloadSize(json.getLong(__download_size__));
                        response.setDownloadUrl(json.getString(__download_url__));
                    } catch (IOException e) {
                        log.error("getDownloadStatusResponseBody failed to open file "+downloadId);
                        log.error("getDownloadStatusResponseBody "+e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    log.error("getDownloadStatusResponseBody no result file "+downloadId);
                    return;
                }
            } else {
                log.error("getDownloadStatusResponseBody failed to find status "+downloadId);
                return;
            }
        }
    }

    public String getDownloadUrl(String downloadId) {
        if(!isValidId(downloadId)) {
            return null;
        }
        if(getStatus(downloadId).equals("done")) {
            if(executorList.containsKey(downloadId)) {
                FileDownloadExecutor executor = executorList.get(downloadId);
                if(executor==null) {
                    log.error("getDownloadUrl: null executor "+downloadId);
                    return null;
                } else {
                    CollectType collectType = CollectType.valueOf(executor.getFtpType());
                    if (collectType == CollectType.vftp_compat) {
                        return "/rss/api/vftp/compat/storage/" + downloadId;
                    } else if (collectType == CollectType.vftp_sss) {
                        return "/rss/api/vftp/sss/storage/" + downloadId;
                    } else {
                        return "/rss/api/ftp/storage/" + downloadId;
                    }
                }
            } else {
                JSONObject json = getResultJson(downloadId);
                if(json==null) {
                    log.error("getDownloadUrl: failed to find download result");
                    return null;
                }
                return json.getString(__download_url__);
            }
        }
        return "";
    }

    public String getStatus(@NonNull final String dlId) {

        if(!executorList.containsKey(dlId)) {
            JSONObject json = getResultJson(dlId);
            if(json==null) {
                return STS_INVALID_ID;
            }
            return json.getString(__status__);
        }
        FileDownloadExecutor executor = executorList.get(dlId);
        if(executor==null) {
            log.error("getStatus null executor "+dlId);
            return STS_INVALID_ID;
        }
        String status = executor.getStatus();
        if(status.equalsIgnoreCase("error")) {
            return STS_ERROR;
        } else if(status.equalsIgnoreCase("complete")) {
            return STS_DONE;
        } else if(status.equalsIgnoreCase("compress")) {
            return STS_IN_COMPRESS;
        } else {
            return STS_IN_PROGRESS;
        }
    }

    public long getLastUpdateTime(@NonNull String downloadId) {
        if(!executorList.containsKey(downloadId)) {
            JSONObject json = getResultJson(downloadId);
            if(json==null) {
                return -1;
            }
            return json.getLong(__last_update__);
        }
        FileDownloadExecutor executor = executorList.get(downloadId);
        if(executor==null) {
            log.error("getLastUpdateTime null executor " + downloadId);
            return -1;
        }
        return executor.getLastUpdate();
    }

    public boolean isValidId(@NonNull final String dlId) {
        if(executorList.containsKey(dlId)) {
            return true;
        } else {
            File resultFile = Paths.get(downloadResultDir, dlId, RESULT_FILE_NAME).toFile();
            if(resultFile.exists() && resultFile.isFile()) {
                return true;
            }
        }
        return false;
    }

    public boolean isHeapManaged(final String downloadId) {
        return executorList.containsKey(downloadId);
    }

    public String getDownloadInfo(@NonNull final String dlId) {
        if(!executorList.containsKey(dlId)) {
            JSONObject json = getResultJson(dlId);
            if(json==null) {
                return null;
            }
            return json.getString(__path__);
        }
        FileDownloadExecutor executor = executorList.get(dlId);
        if(executor==null || executor.isRunning()) {
            return null;
        }
        return executor.getDownloadPath();
    }

    public String getBaseDir(@NonNull final String dlId) {
        if(!executorList.containsKey(dlId)) {
            JSONObject json = getResultJson(dlId);
            if(json==null) {
                return null;
            }
            return json.getString(__base_dir__);
        }
        FileDownloadExecutor executor = executorList.get(dlId);
        if(executor==null || executor.isRunning())
            return null;
        return executor.getBaseDir();
    }

    public int getTotalFiles(@NonNull final String dlId) {
        if(!isValidId(dlId)) {
            return 0;
        }
        if(isHeapManaged(dlId)) {
            FileDownloadExecutor executor = executorList.get(dlId);
            if(executor==null) {
                log.error("getTotalFiles: null executor "+dlId);
                return 0;
            }
            return executor.getTotalFiles();
        }
        JSONObject json = getResultJson(dlId);
        return json==null?0:(int)json.getLong(__total_files__);
    }

    public long getDownloadFiles(@NonNull final String dlId) {
        if(!isValidId(dlId)) {
            return 0;
        }
        if(isHeapManaged(dlId)) {
            FileDownloadExecutor executor = executorList.get(dlId);
            if(executor==null) {
                log.error("getDownloadFiles: null executor "+dlId);
                return 0;
            }
            return executor.getDownloadFiles();
        }
        JSONObject json = getResultJson(dlId);
        return json==null?0:json.getLong(__download_files__);
    }

    public long getTotalSize(final String downloadId) {
        if(!isValidId(downloadId)) {
            return 0;
        }
        if(isHeapManaged(downloadId)) {
            FileDownloadExecutor executor = executorList.get(downloadId);
            if(executor==null) {
                log.error("getTotalSize: null executor "+downloadId);
                return 0;
            }
            return executor.getTotalSize();
        }
        JSONObject json = getResultJson(downloadId);
        return json==null?0:json.getLong(__total_size__);
    }

    public long getDownloadSize(final String downloadId) {
        if(!isValidId(downloadId)) {
            return 0;
        }
        if(isHeapManaged(downloadId)) {
            FileDownloadExecutor executor = executorList.get(downloadId);
            if(executor==null) {
                log.error("getDownloadSize: null executor "+downloadId);
                return 0;
            }
            return executor.getDownloadSize();
        }
        JSONObject json = getResultJson(downloadId);
        return json==null?0:json.getLong(__download_size__);
    }
    
    public List<String> getFabs(@NonNull final String dlId) {
        if(!isValidId(dlId)) {
            return null;
        }
        if(isHeapManaged(dlId)) {
            FileDownloadExecutor executor = executorList.get(dlId);
            if(executor==null) {
                log.error("getFabs: null executor "+dlId);
                return null;
            }
            return executor.getFabs();
        }
        JSONObject json = getResultJson(dlId);
        if(json==null) {
            return null;
        }
        JSONArray fabs = json.getJSONArray(__fabs__);
        List list = fabs.toList();
        return list;
    }

    public CollectType getFtpType(String dlId) {
        if(!isValidId(dlId)) {
            return null;
        }
        String ftpType;
        if(isHeapManaged(dlId)) {
            FileDownloadExecutor executor = executorList.get(dlId);
            if(executor==null) {
                log.error("getFtpType: null executor "+dlId);
                return null;
            }
            ftpType = executor.getFtpType();
        } else {
            JSONObject json = getResultJson(dlId);
            if(json==null) {
                return null;
            }
            ftpType = json.getString(__collect_type__);
        }
        return CollectType.valueOf(ftpType);
    }

    public boolean isBetween(String time, String start, String end) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date dtTime = dateFormat.parse(time);
            Date dtStart = dateFormat.parse(start);
            Date dtEnd = dateFormat.parse(end);
            if((dtTime.after(dtStart) && dtTime.before(dtEnd)) || dtTime.compareTo(dtStart)==0 || dtTime.compareTo(dtEnd)==0) {
                return true;
            }
        } catch (ParseException e) {
            log.error("dateTime parse error");
        }
        return false;
    }

    public String getDownloadCacheDir() {
        return downloadCacheDir;
    }

    public String getDownloadResultDir() {
        return downloadResultDir;
    }
    
    public int getFileServiceRetryCount() { return fileServiceRetryCount; }
    public int getFileServiceRetryInterval() { return fileServiceRetryInterval; }

    private JSONObject getResultJson(String downloadId) {
        File resultFile = Paths.get(downloadResultDir, downloadId, RESULT_FILE_NAME).toFile();
        if(!resultFile.exists() || resultFile.isDirectory()) {
            return null;
        }
        try {
            JSONObject json = new JSONObject(FileUtils.readFileToString(resultFile));
            return json;
        } catch (IOException e) {
            log.error("getResultJson: failed to read result file "+downloadId, LogType.exception);
            log.error("getResultJson: "+e.getMessage(), LogType.exception);
            return null;
        }
    }

    private final EspLog log = new EspLog(getClass());
}
