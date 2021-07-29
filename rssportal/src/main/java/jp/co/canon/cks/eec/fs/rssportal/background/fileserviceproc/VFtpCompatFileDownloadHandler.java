package jp.co.canon.cks.eec.fs.rssportal.background.fileserviceproc;

import jp.co.canon.ckbs.eec.fs.collect.FileServiceCollectConnector;
import jp.co.canon.ckbs.eec.fs.collect.FileServiceCollectConnectorFactory;
import jp.co.canon.ckbs.eec.fs.collect.controller.param.VFtpCompatDownloadRequestResponse;
import jp.co.canon.ckbs.eec.fs.collect.model.VFtpCompatDownloadRequest;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnector;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.ConfigurationService;
import jp.co.canon.cks.eec.fs.rssportal.background.FileDownloadContext;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;

public class VFtpCompatFileDownloadHandler implements FileDownloadHandler {

    private final EspLog log = new EspLog(getClass());
    private final FileServiceManageConnector connector;
    private final FileServiceCollectConnectorFactory fileServiceCollectConnectorFactory;
    private final ConfigurationService configurationService;
    private final String machine;
    private final String command;
    private boolean achieve;

    private String request;

    public VFtpCompatFileDownloadHandler(FileServiceManageConnector connector,
                                         FileServiceCollectConnectorFactory fileServiceCollectConnectorFactory,
                                         ConfigurationService configurationService,
                                         String machine, String command) {
        this.connector = connector;
        this.fileServiceCollectConnectorFactory = fileServiceCollectConnectorFactory;
        this.configurationService = configurationService;
        this.machine = machine;
        this.command = command;
        achieve = true;
    }

    @Override
    public String createDownloadRequest() {
        VFtpCompatDownloadRequestResponse response =
                connector.createVFtpCompatDownloadRequest(machine, command, achieve);
        if(response.getErrorMessage()!=null) {
            return null;
        }
        request = response.getRequest().getRequestNo();
        return request;
    }

    @Override
    public void cancelDownloadRequest() {
        if(request!=null) {
            log.info("cancel to request download " + request);
            connector.cancelAndDeleteVFtpCompatDownloadRequest(machine, request);
        }
    }

    @Override
    public FileDownloadInfo getDownloadedFiles() {
        if(request==null)
            return null;

        VFtpCompatDownloadRequest download = getDownloadRequest();
        if(download==null)
            return null;

        FileDownloadInfo info = new FileDownloadInfo();
        info.setRequestFiles(1);
        info.setRequestBytes(0);
        VFtpCompatDownloadRequest.Status status = download.getStatus();

        if(status==VFtpCompatDownloadRequest.Status.ERROR) {
            log.error("getDownloadedFiles: status error");
            info.setError(true);
        } else if(status==VFtpCompatDownloadRequest.Status.EXECUTED) {
            info.setDownloadFiles(1);
            info.setDownloadBytes(download.getFile().getSize());
            info.setFinish(true);
        } else {
            info.setDownloadBytes(download.getFile().getSize());
        }
        return info;
    }

    @Override
    public String getOutputFileName(FileDownloadContext context) {
        return null;
    }

    @Override
    public String downloadFile(String dest) {
        String host = configurationService.getFileServiceHost(machine);
        FileServiceCollectConnector fscConnector = fileServiceCollectConnectorFactory.getConnector(host);
        return fscConnector.downloadFile(request, dest);
    }

    private VFtpCompatDownloadRequest getDownloadRequest() {
        VFtpCompatDownloadRequestResponse response = connector.getVFtpCompatDownloadRequest(machine, request);
        if(response==null || response.getErrorMessage()!=null) {
            log.error("getDownloadRequest: download response error "+response!=null?response.getErrorMessage():"");
            return null;
        }
        return response.getRequest();
    }
}
