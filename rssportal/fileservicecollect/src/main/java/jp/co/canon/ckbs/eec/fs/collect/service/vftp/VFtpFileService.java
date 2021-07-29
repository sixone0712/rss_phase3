package jp.co.canon.ckbs.eec.fs.collect.service.vftp;

import jp.co.canon.ckbs.eec.fs.collect.model.RequestFileInfo;
import jp.co.canon.ckbs.eec.fs.collect.model.VFtpCompatDownloadRequest;
import jp.co.canon.ckbs.eec.fs.collect.model.VFtpSssDownloadRequest;
import jp.co.canon.ckbs.eec.fs.collect.model.VFtpSssListRequest;
import jp.co.canon.ckbs.eec.fs.collect.service.FileServiceCollectException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class VFtpFileService {
    @Autowired
    VFtpDownloadService downloadService;

    @Autowired
    VFtpListService listService;

    public VFtpSssListRequest addSssListRequest(String machine, String directory, String host, String user, String password) throws FileServiceCollectException {
        VFtpSssListRequest request = new VFtpSssListRequest();
        request.setMachine(machine);
        request.setDirectory(directory);
        return listService.addListRequest(request, host, user, password);
    }

    public VFtpSssListRequest getSssListRequest(String requestNo) throws FileServiceCollectException {
        VFtpSssListRequest request = listService.getListRequest(requestNo);
        if (request == null){
            log.error("request is not found ({})", requestNo);
            throw new FileServiceCollectException(400, "request is not found ("+requestNo+")");
        }
        return request;
    }

    public void cancelAndDeleteSssListRequest(String requestNo){
        listService.cancelAndDeleteSssListRequest(requestNo);
    }

    public VFtpSssDownloadRequest addSssDownloadRequest(String machine,
                                                        String directory,
                                                        String[] fileList,
                                                        boolean archive,
                                                        String host,
                                                        String user,
                                                        String password) throws Exception {
        VFtpSssDownloadRequest request = new VFtpSssDownloadRequest();
        request.setMachine(machine);
        request.setDirectory(directory);
        request.setArchive(archive);
        List<RequestFileInfo> requestFileInfoList = new ArrayList<>();
        for (String filename : fileList){
            RequestFileInfo fileInfo = new RequestFileInfo();
            fileInfo.setName(filename);
            requestFileInfoList.add(fileInfo);
        }
        request.setFileList(requestFileInfoList.toArray(new RequestFileInfo[0]));

        return downloadService.addSssDownloadRequest(request, host, user, password);
    }

    public VFtpSssDownloadRequest getSssDownloadRequest(String requestNo) throws FileServiceCollectException {
        VFtpSssDownloadRequest request = downloadService.getSssDownloadRequest(requestNo);
        if (request == null){
            log.error("request is not found ({})", requestNo);
            throw new FileServiceCollectException(400, "request is not found ("+requestNo+")");
        }
        return request;
    }

    public void cancelAndDeleteSssDownloadRequest(String requestNo){
        downloadService.cancelSssDownloadRequest(requestNo);
    }

    public VFtpCompatDownloadRequest addCompatDownloadRequest(String machine,
                                                              String filename,
                                                              boolean archive,
                                                              String host,
                                                              String user,
                                                              String password) throws Exception {
        VFtpCompatDownloadRequest request = new VFtpCompatDownloadRequest();
        request.setMachine(machine);
        RequestFileInfo fileInfo = new RequestFileInfo();
        fileInfo.setName(filename);
        request.setFile(fileInfo);
        request.setArchive(archive);

        return downloadService.addCompatDownloadRequest(request, host, user, password);
    }

    public VFtpCompatDownloadRequest getCompatDownloadRequest(String requestNo) throws FileServiceCollectException {
        VFtpCompatDownloadRequest request = downloadService.getCompatDownloadRequest(requestNo);
        if (request == null){
            log.error("request is not found ({})", requestNo);
            throw new FileServiceCollectException(400, "request is not found ("+requestNo+")");
        }
        return request;
    }

    public void cancelAndDeleteCompatDownloadRequest(String requestNo){
        downloadService.cancelCompatDownloadRequest(requestNo);
    }

}
