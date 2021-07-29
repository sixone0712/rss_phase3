package jp.co.canon.cks.eec.fs.rssportal.downloadlist;

import lombok.Data;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class DownloadListVo {

    public static final String MACHINE_ALL = "All";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private int id;
    private Timestamp created;
    private String status;
    private int planId;
    private String path;
    private String title;
    private String machine;

    public DownloadListVo() {}

    public DownloadListVo(Timestamp created, String status, int planId, String path) {
        this(created, status, planId, path, "All");
    }

    public DownloadListVo(Timestamp created, String status, int planId, String path, String machine) {
        this.created = created;
        this.status = status;
        this.planId = planId;
        this.path = path;
        this.title = createTitle();
        this.machine = machine;
    }

    private String createTitle() {
        if(created==null)
            return null;
        return dateFormat.format(created.getTime());
    }
}
