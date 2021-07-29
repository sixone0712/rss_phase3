package jp.co.canon.ckbs.eec.service;

public interface DownloadStatusCallback {
    void downloadStart(String fileName);
    void downloadProgress(String fileName, long fileSize);
    void downloadCompleted(String fileName);
    void archiveCompleted(String archiveFileName, long fileSize);
}
