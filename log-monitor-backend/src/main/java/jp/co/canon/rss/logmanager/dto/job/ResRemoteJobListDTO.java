package jp.co.canon.rss.logmanager.dto.job;

import io.swagger.v3.oas.annotations.media.Schema;
import jp.co.canon.rss.logmanager.vo.SiteVo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResRemoteJobListDTO {
    private Integer jobId;
    private int siteId;
    private boolean stop;
    private String companyName;
    private String fabName;
    @Schema(description = "success, failure, notbuild, processing, canceled")
    private String collectStatus;
    @Schema(description = "success, failure, notbuild, processing, canceled")
    private String errorSummaryStatus;
    @Schema(description = "success, failure, notbuild, processing, canceled")
    private String crasDataStatus;
    @Schema(description = "success, failure, notbuild, processing, canceled")
    private String mpaVersionStatus;

    public ResRemoteJobListDTO(Integer jobId, int siteId, boolean stop, SiteVo siteVoList,
                               String collectStatus, String errorSummaryStatus, String crasDataStatus, String mpaVersionStatus) {
        this.jobId = jobId;
        this.siteId = siteId;
        this.stop = stop;
        this.companyName = siteVoList.getCrasCompanyName();
        this.fabName = siteVoList.getCrasFabName();
        this.collectStatus = collectStatus;
        this.errorSummaryStatus = errorSummaryStatus;
        this.crasDataStatus = crasDataStatus;
        this.mpaVersionStatus = mpaVersionStatus;
    }
}
