package jp.co.canon.ckbs.eec.fs.collect.service.ftp;

import jp.co.canon.ckbs.eec.fs.collect.model.DefaultFileInfoModel;
import jp.co.canon.ckbs.eec.fs.collect.model.FileInfoModel;
import jp.co.canon.ckbs.eec.service.command.ListCommand;
import jp.co.canon.ckbs.eec.service.command.LogFileInfo;
import jp.co.canon.ckbs.eec.service.exception.FtpConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FtpListService {
    private static final String FTP_ERROR_CODE = "ERR-0200";
    private static final String COMMEND_SUCCESS = "Command Successful";
    private static final String COMMEND_ERROR = "Command Error";
    private static final String DELIM = ",";
    private static final String DATE_FORMAT = "yyyyMMddHHmmss";

    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    public List<FileInfoModel> getServerFileListInDir(String url,
                                                      String pattern,
                                                      String startdate,
                                                      String enddate,
                                                      String user,
                                                      String password,
                                                      String dir,
                                                      String keyword,
                                                      boolean recursive) throws FtpConnectionException {
        List<FileInfoModel> fileInfoModelList = new ArrayList<>();

        long listStartTime = System.currentTimeMillis();
        log.info("List Start at {}", listStartTime);

        ListCommand listCommand = new ListCommand();
        LogFileInfo[] logFileInfos = listCommand.execute(url, pattern, startdate, enddate, user, password, dir, keyword, recursive);
        if(logFileInfos != null){
            for(LogFileInfo logFileInfo : logFileInfos){
                if (logFileInfo.getIsFile()) {
                    DefaultFileInfoModel fileInfoModel = new DefaultFileInfoModel();
                    fileInfoModel.setName(logFileInfo.getName());
                    fileInfoModel.setSize(logFileInfo.getSize());
                    fileInfoModel.setType("F");
                    fileInfoModel.setExists(true);
                    fileInfoModel.setTimestamp(logFileInfo.getTimestampStr());
                    fileInfoModelList.add(fileInfoModel);
                } else {
                    DefaultFileInfoModel fileInfoModel = new DefaultFileInfoModel();
                    fileInfoModel.setName(logFileInfo.getName());
                    fileInfoModel.setSize(0);
                    fileInfoModel.setType("D");
                    fileInfoModel.setExists(true);
                    fileInfoModel.setTimestamp(logFileInfo.getTimestampStr());
                    fileInfoModelList.add(fileInfoModel);
                }
            }
        }
        long listEndTime = System.currentTimeMillis();
        log.info("List End at {}, spent {}", listEndTime, listEndTime - listStartTime);
        return fileInfoModelList;
    }
}
