package jp.co.canon.ckbs.eec.fs.manage;

import jp.co.canon.ckbs.eec.fs.collect.controller.param.*;
import jp.co.canon.ckbs.eec.fs.collect.service.LogFileList;
import jp.co.canon.ckbs.eec.fs.manage.service.CategoryList;
import jp.co.canon.ckbs.eec.fs.manage.service.MachineList;

public interface FileServiceManageConnector {
    MachineList getMachineList();

    CategoryList getCategoryList();
    CategoryList getCategoryList(String machine);
    LogFileList getFtpFileList(String machine, String category, String from, String to, String keyword, String path);
    LogFileList getFtpFileList(String machine, String category, String from, String to, String keyword, String path, boolean recursive);

    FtpDownloadRequestResponse createFtpDownloadRequest(String machine, String category, boolean archive, String[] fileList);
    FtpDownloadRequestListResponse getFtpDownloadRequestList(String machine, String requestNo);
    void cancelAndDeleteRequest(String machine, String requestNo);

    VFtpSssListRequestResponse createVFtpSssListRequest(String machine, String directory);
    VFtpSssListRequestResponse getVFtpSssListRequest(String machine, String requestNo);
    void cancelAndDeleteVFtpSssListRequest(String machine, String requestNo);

    VFtpSssDownloadRequestResponse createVFtpSssDownloadRequest(String machine, String directory, String[] fileList, boolean archive);
    VFtpSssDownloadRequestResponse getVFtpSssDownloadRequest(String machine, String requestNo);
    void cancelAndDeleteVFtpSssDownloadRequest(String machine, String requestNo);

    VFtpCompatDownloadRequestResponse createVFtpCompatDownloadRequest(String machine, String filename, boolean archive);
    VFtpCompatDownloadRequestResponse getVFtpCompatDownloadRequest(String machine, String requestNo);
    void cancelAndDeleteVFtpCompatDownloadRequest(String machine, String requestNo);

    MachineStatusRequestResponse getOtsStatus(String ots);
    MachineStatusRequestResponse getMachineStatus(String machine);
}
