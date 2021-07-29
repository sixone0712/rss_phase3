package jp.co.canon.ckbs.eec.servicemanager.connector;

import jp.co.canon.ckbs.eec.servicemanager.controller.RestartResponse;
import jp.co.canon.ckbs.eec.servicemanager.service.*;
import org.springframework.core.io.InputStreamResource;

public interface ServiceManagerConnector {
    SystemInfo getSystemInfo();

    RestartResponse restartSystem(LoginInfo loginInfo);

    RestartResponse restartContainers();

    LogFileList getFileList() throws Exception;

    CreateDownloadRequestResult createDownloadRequest(LogFileList logFileList) throws Exception;

    DownloadRequestResult getDownloadRequest(String requestNo) throws Exception;

    void deleteDownloadRequest(String requestNo) throws Exception;

    DownloadStreamInfo downloadFile(String requestNo) throws Exception;
}
