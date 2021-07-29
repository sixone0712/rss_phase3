package jp.co.canon.ckbs.eec.servicemanager.service;

import jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure.OtsInfo;
import jp.co.canon.ckbs.eec.servicemanager.connector.ServiceManagerConnector;
import jp.co.canon.ckbs.eec.servicemanager.connector.ServiceManagerConnectorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

@Service
public class FileDownloadService {
    @Autowired
    ServiceManagerConnectorFactory connectorFactory;

    @Autowired
    DownloadRequestRepository downloadRequestRepository;

    @Autowired
    SystemService systemService;

    @Value("${servicemanager.type}")
    String systemType;

    Map<String, DownloadRequestThread> downloadRequestThreadMap = new HashMap<>();

    public synchronized void addDownloadRequestThread(String requestNo, DownloadRequestThread th){
        downloadRequestThreadMap.put(requestNo, th);
    }

    public synchronized DownloadRequestThread getDownloadReuestThread(String requestNo){
        return downloadRequestThreadMap.get(requestNo);
    }

    public synchronized void removeDownloadRequestThread(String requestNo){
        downloadRequestThreadMap.remove(requestNo);
    }

    public LogFileList getFileList(String device) throws Exception{
        if (systemType.equals("ESP")) {
            if (device == null || device.equals("ESP")){
                return getFileListInThisDevice();
            }
            OtsInfo info = systemService.getOtsInfo(device);
            if (info != null){
                ServiceManagerConnector connector = connectorFactory.getConnector(info.getHost(), info.getPort());
                return connector.getFileList();
            }
            else {
                return new LogFileList();
            }
        } else {
            return getFileListInThisDevice();
        }
    }

    public CreateDownloadRequestResult createDownloadRequest(String device, LogFileList logFileList) throws Exception{
        if (systemType.equals("ESP")) {
            if (device == null || device.equals("ESP")){
                return createDownloadRequestInThisDevice(logFileList);
            }
            OtsInfo info = systemService.getOtsInfo(device);
            if (info != null){
                CreateDownloadRequestResult result = null;
                ServiceManagerConnector connector = connectorFactory.getConnector(info.getHost(), info.getPort());
                result = connector.createDownloadRequest(logFileList);
                if (result.getRequestNo() != null){
                    result.setRequestNo(String.format("%s_%s", result.getRequestNo(), device));
                }
                return result;
            }
            CreateDownloadRequestResult result = new CreateDownloadRequestResult();
            result.setErrorCode(400);
            result.setErrorMessage("Bad Request(Cannot find device)");
            return result;
        } else {
            return createDownloadRequestInThisDevice(logFileList);
        }
    }

    public DownloadRequestResult getDownloadRequest(String requestNo) throws Exception{
        int underIdx = requestNo.indexOf("_");
        if (underIdx == -1){
            return getDownloadRequestInThisDevice(requestNo);
        } else {
            String device = requestNo.substring(underIdx + 1);
            String remoteRequestNo = requestNo.substring(0, underIdx);
            OtsInfo info = systemService.getOtsInfo(device);
            if (info != null){
                DownloadRequestResult result = null;
                ServiceManagerConnector connector = connectorFactory.getConnector(info.getHost(), info.getPort());
                result = connector.getDownloadRequest(remoteRequestNo);
                if (result.getRequestNo() != null){
                    result.setRequestNo(requestNo);
                }
                if (result.getUrl() != null){
                    result.setUrl(String.format("/servicemanager/api/files/storage/%s", result.getRequestNo()));
                }
                return result;
            }
            DownloadRequestResult result = new DownloadRequestResult();
            result.setErrorCode(400);
            result.setErrorMessage("Bad Request(Cannot find device)");
            return result;
        }
    }

    public DownloadStreamInfo getDownloadStreamInfo(String requestNo) throws Exception{
        int underIdx = requestNo.indexOf("_");
        if (underIdx == -1){
            return getDownloadStreamInfoInThisDevice(requestNo);
        } else {
            String device = requestNo.substring(underIdx + 1);
            String remoteRequestNo = requestNo.substring(0, underIdx);
            OtsInfo info = systemService.getOtsInfo(device);
            if (info != null){
                ServiceManagerConnector connector = connectorFactory.getConnector(info.getHost(), info.getPort());
                return connector.downloadFile(remoteRequestNo);
            }
            DownloadStreamInfo streamInfo = new DownloadStreamInfo();
            streamInfo.setErrorCode(400);
            return streamInfo;
        }
    }

    public void deleteDownloadRequest(String requestNo) throws Exception{
        int underIdx = requestNo.indexOf("_");
        if (underIdx == -1){
            deleteDownloadRequestInThisDevice(requestNo);
        } else {
            String device = requestNo.substring(underIdx + 1);
            String remoteRequestNo = requestNo.substring(0, underIdx);
            OtsInfo info = systemService.getOtsInfo(device);
            if (info != null) {
                ServiceManagerConnector connector = connectorFactory.getConnector(info.getHost(), info.getPort());
                connector.deleteDownloadRequest(remoteRequestNo);
            }
            // Bad Request..
        }
    }

    static String getLogType(String dirPath){
        String logType = "etc";
        if(dirPath.startsWith("user")){
            logType = "user";
        } else if (dirPath.startsWith("control")){
            logType = "control";
        } else if (dirPath.startsWith("download")){
            logType = "download";
        } else if (dirPath.startsWith("exception")){
            logType = "exception";
        } else if (dirPath.startsWith("subsystem")){
            logType = "subsystem";
        } else if (dirPath.startsWith("tomcat")){
            logType = "tomcat";
        }
        return logType;
    }

    LogFileList getFileListInThisDevice(){
        File rootDir = new File("/CANON/DEVLOG");
        LogFileList logFileList = new LogFileList();
        ArrayList<LogFileInfo> logFileInfoArrayList = new ArrayList<>();

        Queue<String> directoryQueue = new LinkedList<>();
        directoryQueue.add("");

        String currentDirName = null;
        while((currentDirName = directoryQueue.poll()) != null){
            File dir = new File(rootDir, currentDirName);
            File[] files = dir.listFiles();
            for(File file : files){
                if (file.isFile()) {
                    LogFileInfo info = new LogFileInfo();
                    if (currentDirName.length() == 0){
                        info.setFileType("etc");
                        info.setFileName(file.getName());
                    } else {
                        String logType = getLogType(currentDirName);
                        info.setFileType(logType);
                        info.setFileName(String.format("%s/%s", currentDirName, file.getName()));
                    }
                    info.setFileSize(file.length());
                    logFileInfoArrayList.add(info);
                } else if (file.isDirectory()){
                    if (currentDirName.length() == 0){
                        directoryQueue.add(file.getName());
                    } else {
                        directoryQueue.add(String.format("%s/%s", currentDirName, file.getName()));
                    }
                }
            }
        }

        logFileList.setList(logFileInfoArrayList.toArray(new LogFileInfo[0]));
        return logFileList;
    }

    CreateDownloadRequestResult createDownloadRequestInThisDevice(LogFileList logFileList){
        ArrayList<String> filenameList = new ArrayList<>();
        for (LogFileInfo info: logFileList.getList()){
            filenameList.add(info.getFileName());
        }
        DownloadRequest downloadRequest = new DownloadRequest();
        downloadRequest.setFileNames(filenameList.toArray(new String[0]));
        downloadRequestRepository.addRequest(downloadRequest);
        downloadRequest.setStatus("wait");

        DownloadRequestThread th = new DownloadRequestThread(this, downloadRequest, downloadRequestRepository);
        addDownloadRequestThread(downloadRequest.getRequestNo(), th);
        th.start();

        CreateDownloadRequestResult result = new CreateDownloadRequestResult();
        result.setRequestNo(downloadRequest.getRequestNo());
        return result;
    }

    DownloadRequestResult getDownloadRequestInThisDevice(String requestNo){
        DownloadRequestResult result = new DownloadRequestResult();
        DownloadRequest request = downloadRequestRepository.getRequest(requestNo);
        if (request != null){
            result.setRequestNo(request.getRequestNo());
            result.setStatus(request.getStatus());
            result.setUrl(request.getUrl());
        } else {
            result.setErrorCode(404);
            result.setErrorMessage("Bad Request(Request is not found.)");
        }
        return result;
    }

    DownloadStreamInfo getDownloadStreamInfoInThisDevice(String requestNo){
        DownloadStreamInfo streamInfo = new DownloadStreamInfo();
        DownloadRequest request = downloadRequestRepository.getRequest(requestNo);
        if (request != null){
            String archiveFilePath = downloadRequestRepository.getArchiveFilePath(requestNo);
            File archiveFile = new File(archiveFilePath);
            try {
                FileInputStream is = new FileInputStream(archiveFile);
                streamInfo.setContentLength(archiveFile.length());
                streamInfo.setInputStream(is);
                return streamInfo;
            } catch (FileNotFoundException e) {
            }
        }
        streamInfo.setErrorCode(404);
        return streamInfo;
    }

    void deleteDownloadRequestInThisDevice(String requestNo){
        DownloadRequestThread th = getDownloadReuestThread(requestNo);
        if (th != null){
            th.setStopped(true);
        }
        downloadRequestRepository.deleteRequest(requestNo);
    }
}
