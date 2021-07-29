package jp.co.canon.cks.eec.fs.rssportal.background.fileserviceproc;

import jp.co.canon.ckbs.eec.fs.collect.FileServiceCollectConnector;
import jp.co.canon.ckbs.eec.fs.collect.FileServiceCollectConnectorFactory;
import jp.co.canon.ckbs.eec.fs.collect.controller.param.VFtpSssDownloadRequestResponse;
import jp.co.canon.ckbs.eec.fs.collect.model.RequestFileInfo;
import jp.co.canon.ckbs.eec.fs.collect.model.VFtpSssDownloadRequest;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnector;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.ConfigurationService;
import jp.co.canon.cks.eec.fs.rssportal.background.FileDownloadContext;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;

public class VFtpSssFileDownloadHandler implements FileDownloadHandler {

    private final EspLog log = new EspLog(getClass());
    private final FileServiceManageConnector connector;
    private final FileServiceCollectConnectorFactory fileServiceCollectConnectorFactory;
    private final ConfigurationService configurationService;
    private final String machine;
    private final String directory;
    private final String[] files;
    private boolean achieve;

    private String request;

    public VFtpSssFileDownloadHandler(FileServiceManageConnector connector,
                                      FileServiceCollectConnectorFactory fileServiceCollectConnectorFactory,
                                      ConfigurationService configurationService,
                                      String machine, String directory, String[] files) {
        this.connector = connector;
        this.fileServiceCollectConnectorFactory = fileServiceCollectConnectorFactory;
        this.configurationService = configurationService;
        this.machine = machine;
        this.directory = directory;
        this.files = files;
        achieve = true;
    }

    @Override
    public String createDownloadRequest() {
        VFtpSssDownloadRequestResponse response = connector.createVFtpSssDownloadRequest(machine, directory, files, true);
        if(response.getErrorMessage()!=null)
            return null;
        request = response.getRequest().getRequestNo();
        return request;
    }

    @Override
    public void cancelDownloadRequest() {
        if(request!=null) {
            log.info("cancel to request download " + request);
            connector.cancelAndDeleteVFtpSssDownloadRequest(machine, request);
        }
    }

    @Override
    public FileDownloadInfo getDownloadedFiles() {
        if(request==null)
            return null;
        VFtpSssDownloadRequest download = getDownloadRequest();
        if(download!=null) {
            FileDownloadInfo info = new FileDownloadInfo();
            info.setRequestFiles(files.length);
            info.setRequestBytes(0);
            if(download.getStatus()==VFtpSssDownloadRequest.Status.ERROR) {
                log.error("getDownloadedFiles: status error");
                info.setError(true);
            } else if(download.getStatus()==VFtpSssDownloadRequest.Status.EXECUTED) {
                long bytes=0;
                for(RequestFileInfo file: download.getFileList()) {
                    bytes += file.getSize();
                }
                info.setDownloadFiles(files.length);
                info.setDownloadBytes(bytes);
                info.setFinish(true);
            } else {
                long files=0, bytes=0;
                for(RequestFileInfo file: download.getFileList()) {
                    if(file.isDownloaded()) {
                        ++files;
                    }
                    bytes += file.getSize();
                }
                info.setDownloadFiles(files);
                info.setDownloadBytes(bytes);
            }
            return info;
        }
        return null;
    }

    @Override
    public String getOutputFileName(FileDownloadContext context) {
        return context.getDirectory()+".zip";
    }

    @Override
    public String downloadFile(String dest) {
        String host = configurationService.getFileServiceHost(machine);
        FileServiceCollectConnector fscConnector = fileServiceCollectConnectorFactory.getConnector(host);
        return fscConnector.downloadFile(request, dest);
    }

    private VFtpSssDownloadRequest getDownloadRequest() {
        VFtpSssDownloadRequestResponse response = connector.getVFtpSssDownloadRequest(machine, request);
        if(response==null || response.getErrorMessage()!=null) {
            log.error("getDownloadRequest: download response error "+response!=null?response.getErrorMessage():"");
            return null;
        }
        return response.getRequest();
    }
}
