package jp.co.canon.cks.eec.fs.rssportal.model.users;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserInfoFile {
	String username;
	String password;
	List<String> permission;
}
