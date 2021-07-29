package jp.co.canon.cks.eec.fs.rssportal.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import jp.co.canon.cks.eec.fs.rssportal.Defines.UserPermission;
import jp.co.canon.cks.eec.fs.rssportal.common.Tool;
import jp.co.canon.cks.eec.fs.rssportal.model.users.UserInfoFile;
import jp.co.canon.cks.eec.fs.rssportal.service.UserService;
import jp.co.canon.cks.eec.fs.rssportal.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ServiceManagerController {
	private final UserService serviceUser;
	@Value("${rssportal.admin-init-password-filename}")
	private String adminInitPasswordFilename;

	@Autowired
	public ServiceManagerController(UserService serviceUser) {
		this.serviceUser = serviceUser;
	}

	public boolean isSystemUser(List<String> permission) {
		for(String item : permission) {
			if(item.equals(UserPermission.SYSTEM_LOG_DOWNLOAD) || item.equals(UserPermission.SYSTEM_RESTART)) {
				return true;
			}
		}
		return false;
	}

	public void saveJsonFile(List<UserInfoFile> list) throws IOException {
		Gson gsonWrite = new GsonBuilder().setPrettyPrinting().create();
		String jsonString = gsonWrite.toJson(list);

		FileWriter writeFile = new FileWriter(adminInitPasswordFilename, false);
		writeFile.write(jsonString);
		writeFile.flush();
		writeFile.close();
	}

	public void addUserToFile(String username, String password, List<String> permission) {
		try {
			Gson gsonRead = new Gson();
			JsonReader jsonReader = new JsonReader(new FileReader(adminInitPasswordFilename));
			List<UserInfoFile> list = gsonRead.fromJson(jsonReader, new TypeToken<List<UserInfoFile>>(){}.getType());

			UserInfoFile addUser = new UserInfoFile();
			addUser.setUsername(username);
			addUser.setPassword(password);
			addUser.setPermission(permission);
			list.add(addUser);
			saveJsonFile(list);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void editUserToFile(String username, String password, List<String> permission) {
		try {
			Gson gsonRead = new Gson();
			JsonReader jsonReader = new JsonReader(new FileReader(adminInitPasswordFilename));
			List<UserInfoFile> list = gsonRead.fromJson(jsonReader, new TypeToken<List<UserInfoFile>>(){}.getType());

			List<UserInfoFile> newList = list.stream().map(item -> {
				if(item.getUsername().equals(username)) {
					item.setPassword(password);
					item.setPermission(permission);
				}
				return item;
			}).collect(Collectors.toList());

			saveJsonFile(newList);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void createUserInfoToFile() {
		List<UserVo> filteredList = serviceUser.getUserList().stream()
				.filter(item -> isSystemUser(Tool.toJavaList(item.getPermissions())))
				.collect(Collectors.toList());

		List<UserInfoFile> newList = new ArrayList<UserInfoFile>();

		for(UserVo item : filteredList) {
			UserInfoFile user = new UserInfoFile();
			user.setUsername(item.getUsername());
			user.setPassword(item.getPassword());
			user.setPermission(Tool.toJavaList(item.getPermissions()));
			newList.add(user);
		}
		try {
			saveJsonFile(newList);
		}  catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void deleteUserToFile(String username) {
		try {
			Gson gsonRead = new Gson();
			JsonReader jsonReader = new JsonReader(new FileReader(adminInitPasswordFilename));
			List<UserInfoFile> list = gsonRead.fromJson(jsonReader, new TypeToken<List<UserInfoFile>>() {
			}.getType());
			List<UserInfoFile> newList = list.stream().filter(item -> !item.getUsername().equals(username)).collect(Collectors.toList());
			saveJsonFile(newList);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void immigratePermission() {
		List<UserVo> userList = serviceUser.getUserList();
		for(UserVo user : userList) {
			switch (user.getPermissions()) {
				case "100":
					user.setPermissions(Tool.toCSVString(Arrays.asList(
							UserPermission.MANUAL_DOWNLOAD_VFTP,
							UserPermission.AUTO_COLLECTION_SETTING,
							UserPermission.SYSTEM_LOG_DOWNLOAD,
							UserPermission.SYSTEM_RESTART,
							UserPermission.ACCOUNT_SETTING,
							UserPermission.CONFIG_SETTING
					)));
					serviceUser.modifyUser(user);
					break;
				case "50":
					user.setPermissions(Tool.toCSVString(Arrays.asList(
							UserPermission.MANUAL_DOWNLOAD_VFTP,
							UserPermission.AUTO_COLLECTION_SETTING
					)));
					serviceUser.modifyUser(user);
					break;
				case "20":
					user.setPermissions(Tool.toCSVString(Arrays.asList(
							UserPermission.MANUAL_DOWNLOAD_VFTP
					)));
					serviceUser.modifyUser(user);
					break;
				case "10":
					user.setPermissions("");
					serviceUser.modifyUser(user);
					break;
				default:
					break;
			}
		}
	}

	public void saveFileUserInfoFromDB() {
		List<UserVo> filteredList = serviceUser.getUserList().stream()
				.filter(item -> isSystemUser(Tool.toJavaList(item.getPermissions())))
				.collect(Collectors.toList());

		List<UserInfoFile> newList = new ArrayList<UserInfoFile>();

		for(UserVo item : filteredList) {
			UserInfoFile user = new UserInfoFile();
			user.setUsername(item.getUsername());
			user.setPassword(item.getPassword());
			user.setPermission(Tool.toJavaList(item.getPermissions()));
			newList.add(user);
		}
		try {
			saveJsonFile(newList);
		}  catch (IOException e) {
			e.printStackTrace();
		}
	}
}
