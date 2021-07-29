package jp.co.canon.cks.eec.fs.rssportal.background.search;

import lombok.Data;

@Data
public class FileSearchInfo {

    private String  searchId;
    private long    searchedCnt;
    private String  resultUrl;
    private String  status;
}
