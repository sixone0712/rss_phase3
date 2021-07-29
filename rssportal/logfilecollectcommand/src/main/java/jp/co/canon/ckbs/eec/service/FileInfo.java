package jp.co.canon.ckbs.eec.service;

public class FileInfo {
    String filename;
    int retry_count = 0;
    String downloadPath;

    public FileInfo(String filename, String downloadPath){
        this.setFilename(filename);
        this.setDownloadPath(downloadPath);
    }

    public void setFilename(String filename){
        this.filename = filename;
    }
    public void setDownloadPath(String path){
        this.downloadPath = path;
    }

    public String getDownloadPath(){
        return this.downloadPath;
    }

    public String getFilename(){
        return  this.filename;
    }

    public void increaseRetryCount(){
        this.retry_count++;
    }

    public int getRetryCount(){
        return retry_count;
    }
}
