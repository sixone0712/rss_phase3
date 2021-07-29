package jp.co.canon.cks.eec.fs.rssportal.dao;

import jp.co.canon.cks.eec.fs.rssportal.vo.ConfigHistoryVo;
import jp.co.canon.cks.eec.fs.rssportal.vo.DownloadHistoryVo;

import java.util.List;
import java.util.Map;

public interface DownloadHistoryDao {

    List<DownloadHistoryVo> findAll();
    int getTotalCnt();
    /*    DownloadHistoryVo find( Map<String, Object> param);*/
    boolean add(DownloadHistoryVo history) ;
    boolean configLogAdd(ConfigHistoryVo param);
    /*public boolean modify(DownloadHistoryVo history);
    boolean delete(DownloadHistoryVo history);*/
}
