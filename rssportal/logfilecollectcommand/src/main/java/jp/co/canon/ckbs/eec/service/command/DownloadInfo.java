package jp.co.canon.ckbs.eec.service.command;

public class DownloadInfo {
    long totalDownloadCount = 0;

    public synchronized void increaseDownloadCount(){
        totalDownloadCount++;
    }

    public long getTotalDownloadCount(){
        return totalDownloadCount;
    }
}
