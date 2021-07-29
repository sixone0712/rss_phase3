package jp.co.canon.ckbs.eec.fs.collect.service.vftp;

import jp.co.canon.ckbs.eec.fs.collect.model.VFtpSssListRequest;
import jp.co.canon.ckbs.eec.fs.collect.service.VFtpFileInfo;
import jp.co.canon.ckbs.eec.fs.collect.service.configuration.FtpServerInfo;
import jp.co.canon.ckbs.eec.service.ListFtpCommand;
import jp.co.canon.ckbs.eec.service.command.LogFileInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
class SssListProcessThread extends Thread{
    VFtpSssListRequest request;
    FtpServerInfo ftpServerInfo;
    VFtpListService listService;
    boolean stopped = false;

    public SssListProcessThread(VFtpSssListRequest request, FtpServerInfo ftpServerInfo, VFtpListService listService){
        this.request = request;
        this.ftpServerInfo = ftpServerInfo;
        this.listService = listService;
    }

    @Override
    public void run() {
        log.trace("process start ({})", request.getRequestNo());
        List<VFtpFileInfo> fileInfoList = new ArrayList<>();

        request.setStatus(VFtpSssListRequest.Status.EXECUTING);
        ListFtpCommand listCommand = new ListFtpCommand();
        LogFileInfo[] ftpFiles = listCommand.execute(ftpServerInfo.getHost(),
                22001,
                ftpServerInfo.getFtpmode(),
                ftpServerInfo.getUser(),
                ftpServerInfo.getPassword(),
                "/VROOT/SSS/Optional",
                request.getDirectory());
        if (stopped){
            return;
        }
        for(LogFileInfo ftpFile : ftpFiles){
            if (ftpFile.getIsFile()) {
                VFtpFileInfo fileInfo = new VFtpFileInfo();
                fileInfo.setFileName(ftpFile.getName());
                fileInfo.setFileSize(ftpFile.getSize());
                fileInfo.setFileType("F");
                fileInfoList.add(fileInfo);
            }
        }
        request.setFileList(fileInfoList.toArray(new VFtpFileInfo[0]));
        if (stopped){
            return;
        }
        request.setStatus(VFtpSssListRequest.Status.EXECUTED);
        request.setCompletedTime(System.currentTimeMillis());
        listService.requestCompleted(request.getRequestNo());
        log.trace("process end ({})", request.getRequestNo());
    }

    public void stopExecute(){
        this.stopped = true;
        log.trace("stop requested. {}", request.getRequestNo());
        request.setStatus(VFtpSssListRequest.Status.CANCEL);
    }
}
