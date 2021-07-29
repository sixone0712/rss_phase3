package jp.co.canon.ckbs.eec.fs.collect.service.ftp;

import jp.co.canon.ckbs.eec.fs.collect.model.FileInfoModel;
import jp.co.canon.ckbs.eec.fs.collect.model.FtpDownloadRequest;
import jp.co.canon.ckbs.eec.fs.collect.model.RequestFileInfo;
import jp.co.canon.ckbs.eec.fs.collect.service.FileInfo;
import jp.co.canon.ckbs.eec.fs.collect.service.LogFileList;
import jp.co.canon.ckbs.eec.service.exception.FtpConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FtpFileService {
    @Autowired
    FtpListService listService;

    @Autowired
    FtpDownloadService downloadService;

    List<FileInfoModel> getList(String url,
                                String pattern,
                                String startDate,
                                String endDate,
                                String user,
                                String password,
                                String path,
                                String keyword,
                                boolean recursive
                                ) throws FtpConnectionException {
        return listService.getServerFileListInDir(url,
                pattern,
                startDate,
                endDate,
                user,
                password,
                path,
                keyword,
                recursive);
    }

    public LogFileList getLogFileList(String url,
                                      String pattern,
                                      String startDate,
                                      String endDate,
                                      String user,
                                      String password,
                                      String path,
                                      String keyword,
                                      boolean recursive) {
        LogFileList out = new LogFileList();

        List<FileInfoModel> list = null;
        try {
            list = this.getList(url, pattern, startDate, endDate, user, password, path, keyword, recursive);
        } catch (FtpConnectionException e) {
            out.setErrorCode("500");
            out.setErrorMessage(e.getMessage());
            return out;
        }
        List<FileInfo> r = new ArrayList<>();
        for (FileInfoModel src : list) {
            FileInfo dst = new FileInfo();
            dst.setFilename(src.getName());
            dst.setSize(src.getSize());
            dst.setTimestamp(src.getTimestamp());
            dst.setType(src.getType());
            r.add(dst);
        }

        out.setList(r.toArray(new FileInfo[0]));
        return out;
    }

    public FtpDownloadRequest addDownloadRequest(String machine,
                                                 String category,
                                                 String[] files,
                                                 boolean archive,
                                                 String host,
                                                 String user,
                                                 String password) throws Exception {
        FtpDownloadRequest request = new FtpDownloadRequest();

        request.setMachine(machine);
        request.setCategory(category);
        RequestFileInfo[] fileInfos = new RequestFileInfo[files.length];
        for(int idx = 0; idx < files.length; ++idx){
            fileInfos[idx] = new RequestFileInfo(files[idx]);
        }
        request.setFileInfos(fileInfos);
        request.setArchive(archive);

        return downloadService.addDownloadRequest(request, host, user, password);
    }

    public FtpDownloadRequest[] getFtpDownloadRequest(String requestNo){
        return downloadService.getFtpDownloadRequest(requestNo);
    }

    public void cancelDownloadRequest(String requestNo){
        downloadService.cancelDownloadRequest(requestNo);
    }
}
