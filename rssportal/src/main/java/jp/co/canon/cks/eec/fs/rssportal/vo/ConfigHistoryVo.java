package jp.co.canon.cks.eec.fs.rssportal.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class ConfigHistoryVo {
    private int idx;
    private String user;
    private Date date;
    private String type;
    private String filename;
    private boolean validity;
}
