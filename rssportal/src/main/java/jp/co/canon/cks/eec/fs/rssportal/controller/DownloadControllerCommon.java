package jp.co.canon.cks.eec.fs.rssportal.controller;

import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnectorFactory;
import jp.co.canon.cks.eec.fs.rssportal.background.FileDownloader;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import jp.co.canon.cks.eec.fs.rssportal.common.LogType;
import jp.co.canon.cks.eec.fs.rssportal.service.JwtService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DownloadControllerCommon {

    private final EspLog log = new EspLog(getClass());
    protected final JwtService jwtService;
    protected final FileDownloader fileDownloader;
    protected final FileServiceManageConnectorFactory connectorFactory;

    public DownloadControllerCommon(JwtService jwtService, FileDownloader fileDownloader,
                                    FileServiceManageConnectorFactory connectorFactory) {
        this.jwtService = jwtService;
        this.fileDownloader = fileDownloader;
        this.connectorFactory = connectorFactory;
    }

    protected String createZipFilename(String downloadId, String username) {
        // format: username_fabname{_fabname2}_YYYYMMDD_hhmmss.zip

        //String username = jwtService.getCurAccTokenUserName();

        if(username==null) {
            log.error("no username", LogType.control);
            return null;
        }

        List<String> fabs = fileDownloader.getFabs(downloadId);
        if(fabs==null || fabs.size()==0) {
            log.error("no fab info", LogType.control);
            return null;
        }
        String fab = fabs.get(0);
        if(fabs.size()>1) {
            for(int i=1; i<fabs.size(); ++i)
                fab += "_"+fabs.get(i);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String cur = dateFormat.format(new Date(System.currentTimeMillis()));

        String fileName = String.format("%s_%s_%s.zip", username, fab, cur);
        log.info("filename = "+fileName, LogType.control);
        return fileName;
    }

    protected  <ValueType> ValueType getObjectFromMap(Map<String, Object> map, String key, ValueType type) throws RuntimeException {
        if(!map.containsKey(key)) {
            throw new RuntimeException("cannot find value");
        }
        return (ValueType) map.get(key);
    }
}
