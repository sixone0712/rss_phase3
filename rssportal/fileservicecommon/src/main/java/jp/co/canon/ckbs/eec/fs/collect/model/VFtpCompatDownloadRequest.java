package jp.co.canon.ckbs.eec.fs.collect.model;

import lombok.Getter;
import lombok.Setter;

public class VFtpCompatDownloadRequest extends FtpRequest {
    @Getter @Setter
    RequestFileInfo file;

    @Getter @Setter
    boolean archive;

    @Getter @Setter
    String archiveFileName;

    @Getter @Setter
    String archiveFilePath;

    public synchronized void downloadProgress(String filename, long size){
        if (file.getName().equals(filename)) {
            file.setSize(size);
        }
    }

    public synchronized void downloadCompleted(String filename){
        if (file.getName().equals(filename)){
            file.setDownloaded(true);
            if (archive == false){
                file.setDownloadPath(this.requestNo + "/" + filename);
            }
        }
    }
}
