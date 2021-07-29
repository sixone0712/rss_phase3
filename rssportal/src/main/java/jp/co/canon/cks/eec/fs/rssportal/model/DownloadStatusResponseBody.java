package jp.co.canon.cks.eec.fs.rssportal.model;

import jp.co.canon.cks.eec.fs.rssportal.background.CollectType;
import jp.co.canon.cks.eec.fs.rssportal.background.FileDownloader;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

@Getter
@Setter
public class DownloadStatusResponseBody {

    private String downloadId;
    private String status = "invalid-id";
    private int totalFiles = -1;
    private long downloadedFiles = -1;
    private long totalSize = -1;
    private long downloadSize = -1;
    private String downloadUrl = "";

    public DownloadStatusResponseBody(@NonNull FileDownloader fileDownloader, @NonNull final String dlId) {
        this.downloadId = dlId;
        fileDownloader.getDownloadStatusResponseBody(this);
    }
}
