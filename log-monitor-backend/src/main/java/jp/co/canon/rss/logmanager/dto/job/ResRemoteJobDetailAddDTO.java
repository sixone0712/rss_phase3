package jp.co.canon.rss.logmanager.dto.job;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ResRemoteJobDetailAddDTO {
    private int siteId;
    private int [] planIds;
    private String [] sendingTimes;
    private Boolean isErrorSummary;
    private Boolean isCrasData;
    private Boolean isMpaVersion;
    private ResMailContextAddDTO errorSummary;
    private ResMailContextAddDTO crasData;
    private ResMailContextAddDTO mpaVersion;
}
