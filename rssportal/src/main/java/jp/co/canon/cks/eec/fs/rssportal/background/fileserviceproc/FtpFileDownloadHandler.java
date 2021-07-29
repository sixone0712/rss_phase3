package jp.co.canon.cks.eec.fs.rssportal.background.fileserviceproc;

import jp.co.canon.ckbs.eec.fs.collect.FileServiceCollectConnector;
import jp.co.canon.ckbs.eec.fs.collect.FileServiceCollectConnectorFactory;
import jp.co.canon.ckbs.eec.fs.collect.controller.param.FtpDownloadRequestListResponse;
import jp.co.canon.ckbs.eec.fs.collect.controller.param.FtpDownloadRequestResponse;
import jp.co.canon.ckbs.eec.fs.collect.model.FtpDownloadRequest;
import jp.co.canon.ckbs.eec.fs.collect.model.RequestFileInfo;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnector;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.ConfigurationService;
import jp.co.canon.cks.eec.fs.rssportal.background.FileDownloadContext;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;

public class FtpFileDownloadHandler implements FileDownloadHandler {

    private final EspLog log = new EspLog(getClass());
    private final FileServiceManageConnector connector;
    private final FileServiceCollectConnectorFactory fileServiceCollectConnectorFactory;
    private final ConfigurationService configurationService;
    private final String machine;
    private final String category;
    private final String[] files;
    private boolean achieve;

    private String request;

    public FtpFileDownloadHandler(FileServiceManageConnector connector,
                                  FileServiceCollectConnectorFactory fileServiceCollectConnectorFactory,
                                  ConfigurationService configurationService,
                                  String machine, String category, String[] files) {
        this.connector = connector;
        this.fileServiceCollectConnectorFactory = fileServiceCollectConnectorFactory;
        this.configurationService = configurationService;
        this.machine = machine;
        this.category = category;
        this.files = files;
        achieve = true;
    }

    @Override
    public String createDownloadRequest() {
        FtpDownloadRequestResponse response = connector.createFtpDownloadRequest(
                machine, category, achieve, files);
        if(response.getErrorCode()!=null) {
            log.error("createDownloadRequest: error  "+response.getErrorMessage());
            return null;
        }
        request = response.getRequestNo();
        return request;
    }

    @Override
    public void cancelDownloadRequest() {
        if(request!=null) {
            log.info("cancel to request download "+request);
            connector.cancelAndDeleteRequest(machine, request);
        }
    }

    @Override
    public FileDownloadInfo getDownloadedFiles() {
        if(request==null) {
            log.error("getDownloadedFiles: null request");
            return null;
        }
        FtpDownloadRequest download = getDownloadRequest();
        if(download!=null) {
            FileDownloadInfo info = new FileDownloadInfo();

            long totalSize = 0, downloadSize = 0;
            for(RequestFileInfo file: download.getFileInfos()) {
                totalSize += file.getSize();
                downloadSize += file.getSize();
            }
            info.setRequestBytes(totalSize);
            info.setDownloadBytes(downloadSize);
            info.setRequestFiles(download.getTotalFileCount());
            info.setDownloadFiles(download.getDownloadedFileCount());

            if(download.getStatus()==FtpDownloadRequest.Status.ERROR) {
                log.error("getDownloadedFiles: error "+download.getErrorMessage());
                info.setError(true);
            } else if(download.getStatus()==FtpDownloadRequest.Status.EXECUTED) {
                log.info("getDownloadedFiles: "+request+" executed");
                info.setFinish(true);
            }
            return info;
        }
        return null;
    }

    @Override
    public String getOutputFileName(FileDownloadContext context) {
        return String.format("%s.zip", context.getLogTypeStr());
    }

    public String downloadFile(String dest) {
        String host = configurationService.getFileServiceHost(machine);
        FileServiceCollectConnector fscConnector = fileServiceCollectConnectorFactory.getConnector(host);
        return fscConnector.downloadFile(request, dest);
    }

    private FtpDownloadRequest getDownloadRequest() {
        if(request==null)
            return null;
        FtpDownloadRequestListResponse response = connector.getFtpDownloadRequestList(machine, request);
        if(response!=null) {
            if(response.getErrorCode()!=null) {
                log.error("getDownloadRequest: error  "+response.getErrorCode());
                return null;
            }
            for (FtpDownloadRequest download : response.getRequestList()) {
                if (download.getRequestNo().equals(request)) {
                    return download;
                }
            }
        }
        log.error("getDownloadRequest: failed to get response");
        return null;
    }
}