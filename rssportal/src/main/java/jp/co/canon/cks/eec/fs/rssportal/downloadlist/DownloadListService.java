package jp.co.canon.cks.eec.fs.rssportal.downloadlist;

import jp.co.canon.cks.eec.fs.rssportal.vo.CollectPlanVo;
import org.springframework.lang.NonNull;

import java.util.List;

public interface DownloadListService {

    boolean isReady();
    DownloadListVo get(int id);
    List<DownloadListVo> getFinishedList();
    List<DownloadListVo> getList(int planId);
    List<DownloadListVo> getList(int planId, int offset, int limit);
    boolean insert(DownloadListVo item);
    boolean insert(CollectPlanVo plan, String filePath, String machine);
    boolean updateDownloadStatus(int id);
    boolean delete(int id);
}
