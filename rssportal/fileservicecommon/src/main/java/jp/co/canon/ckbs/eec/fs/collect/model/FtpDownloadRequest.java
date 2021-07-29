package jp.co.canon.ckbs.eec.fs.collect.model;

import lombok.Getter;
import lombok.Setter;

public class FtpDownloadRequest extends FtpRequest{
    @Getter @Setter
    String category;

    @Getter @Setter
    String result;

    @Getter @Setter
    boolean archive;

    @Getter @Setter
    String archiveFileName;

    @Getter @Setter
    long archiveFileSize;

    @Getter @Setter
    String archiveFilePath;

    RequestFileInfo[] fileInfos;

    @Getter @Setter
    String directory = null;

    @Getter @Setter
    String errorMessage = null;

    @Getter
    long downloadedFileCount = 0;

    @Getter
    long totalFileCount = 0;

    public FtpDownloadRequest(){
        status = Status.WAIT;
    }

    public void fileDownloadCompleted(String fileName){
        RequestFileInfo[] fileList = getFileInfos();
        for (RequestFileInfo info : fileList) {
            if (info.getName().equals(fileName)){
                if (!info.downloaded) {
                    info.setDownloaded(true);
                    /*
                    info.setDownloadPath(directory +"/" + downloadPath);
                     */
                    ++downloadedFileCount;
                }
                break;
            }
        }
    }

    public void fileDownloadProgress(String fileName, long fileSize){
        RequestFileInfo[] fileList = getFileInfos();
        for (RequestFileInfo info : fileList) {
            if (info.getName().equals(fileName)){
                info.setSize(fileSize);
                break;
            }
        }
    }

    public RequestFileInfo[] getFileInfos(){
        return fileInfos;
    }

    public void setFileInfos(RequestFileInfo[] fileInfos){
        this.fileInfos = fileInfos;
        this.totalFileCount = fileInfos.length;
    }

    public static boolean checkCompletedStatus(FtpDownloadRequest request){
        Status sts = request.getStatus();
        if (sts == Status.CANCEL){
            return true;
        }
        if (sts == Status.ERROR){
            return true;
        }
        if (sts == Status.EXECUTED){
            return true;
        }
        return false;
    }
}
