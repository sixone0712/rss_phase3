package jp.co.canon.cks.eec.fs.rssportal.service;

import jp.co.canon.cks.eec.fs.rssportal.vo.ConfigHistoryVo;
import jp.co.canon.cks.eec.fs.rssportal.vo.DownloadHistoryVo;

import java.util.List;

public interface DownloadHistoryService {
    List<DownloadHistoryVo> getHistoryList();
    int getHistoryTotalCnt();
    boolean addDlHistory(DownloadHistoryVo dlHistory);
    boolean addConfigLog(ConfigHistoryVo log);
}
