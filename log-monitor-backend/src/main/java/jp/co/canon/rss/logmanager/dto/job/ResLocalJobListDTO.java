package jp.co.canon.rss.logmanager.dto.job;

import io.swagger.v3.oas.annotations.media.Schema;
import jp.co.canon.rss.logmanager.vo.SiteVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
public class ResLocalJobListDTO {
    private Integer jobId;
    private int siteId;
    private String companyName;
    private String fabName;
    @Schema(description = "success, failure, notbuild, processing, canceled")
    private String collectStatus;
    private int [] fileIndices;
    private String [] fileOriginalNames;
    private LocalDateTime registeredDate;
    private boolean stop;

    public ResLocalJobListDTO(Integer jobId, int siteId, SiteVo siteVoListLocal,
                              String collectStatus, int [] fileIndices, String [] fileOriginalNames,
                              LocalDateTime registeredDate, boolean stop) {
        this.jobId = jobId;
        this.siteId = siteId;
        this.companyName = siteVoListLocal.getCrasCompanyName();
        this.fabName = siteVoListLocal.getCrasFabName();
        this.collectStatus = collectStatus;
        this.fileIndices = fileIndices;
        this.fileOriginalNames = fileOriginalNames;
        this.registeredDate = registeredDate;
        this.stop = stop;
    }
}
