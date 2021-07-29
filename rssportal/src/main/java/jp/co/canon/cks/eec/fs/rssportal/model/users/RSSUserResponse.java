package jp.co.canon.cks.eec.fs.rssportal.model.users;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RSSUserResponse {
    private int userId;
    private String userName;
    private List<String> permission;
    private String created;
    private String modified;
    private String lastAccess;
}
