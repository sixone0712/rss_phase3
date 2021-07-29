package jp.co.canon.ckbs.eec.servicemanager.controller;

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
