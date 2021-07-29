package jp.co.canon.rss.logmanager.dto.job;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ResRemoteJobDetailDTO {
    private int siteId;
    private int [] planIds;
    private String [] sendingTimes;
    private Boolean isErrorSummary;
    private Boolean isCrasData;
    private Boolean isMpaVersion;
    private ResMailContextDTO errorSummary;
    private ResMailContextDTO crasData;
    private ResMailContextDTO mpaVersion;
}
