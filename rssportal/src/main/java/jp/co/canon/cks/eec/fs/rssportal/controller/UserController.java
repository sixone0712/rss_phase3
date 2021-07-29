package jp.co.canon.cks.eec.fs.rssportal.controller;

import jp.co.canon.cks.eec.fs.rssportal.Defines.RSSErrorReason;
import jp.co.canon.cks.eec.fs.rssportal.common.EspLog;
import jp.co.canon.cks.eec.fs.rssportal.common.Tool;
import jp.co.canon.cks.eec.fs.rssportal.model.error.RSSError;
import jp.co.canon.cks.eec.fs.rssportal.model.users.RSSUserResponse;
import jp.co.canon.cks.eec.fs.rssportal.service.JwtService;
import jp.co.canon.cks.eec.fs.rssportal.service.UserService;
import jp.co.canon.cks.eec.fs.rssportal.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final HttpSession httpSession;
    private final UserService serviceUser;
    private final JwtService jwtService;
    private final ServiceManagerController serviceManagerController;
    private final EspLog log = new EspLog(getClass());

    @Value("${rssportal.admin-init-password}")
    private String adminInitPassword;

    @Value("${rssportal.admin-init-password-filename}")
    private String adminInitPasswordFilename;

    @Autowired
    public UserController(HttpSession httpSession, UserService serviceUser, JwtService jwtService, ServiceManagerController serviceManagerController) {
        this.httpSession = httpSession;
        this.serviceUser = serviceUser;
        this.jwtService = jwtService;
        this.serviceManagerController = serviceManagerController;
    }

    @PatchMapping("/{userID}/password")
    @ResponseBody
    public ResponseEntity<?> changePw(HttpServletRequest request, @PathVariable("userID") String userID,
                                      @RequestBody Map<String, Object> param) throws Exception {
        log.info(String.format("[Patch] %s", request.getServletPath()));
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        if (userID == null || userID.equals("")) {
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            log.info(String.format("[Patch] %s / userId is empty.", request.getServletPath()));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        int id = Integer.parseInt(userID);
        String oldPassword = param.containsKey("oldPassword") ? (String) param.get("oldPassword") : null;
        String newPassword = param.containsKey("newPassword") ? (String) param.get("newPassword") : null;

        if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            resBody.put("error", error.getRSSError());
            log.info(String.format("[Patch] %s / oldPassword or newPassword is empty.", request.getServletPath()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
        }

        UserVo userObj = serviceUser.getUser(id);
        if (userObj != null && (userObj.getId() >= 10000)) {
            if (!oldPassword.equals(userObj.getPassword())) {
                error.setReason(RSSErrorReason.INVALID_PASSWORD);
                resBody.put("error", error.getRSSError());
                log.info(String.format("[Patch] %s / oldPassword does not match the password of the DB.", request.getServletPath()));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
            }
            userObj.setPassword(newPassword);
            if (serviceUser.modifyUser(userObj)) {
                if(Tool.isSystemUser(Tool.toJavaList(userObj.getPermissions()))) {
                    serviceManagerController.editUserToFile(userObj.getUsername(), newPassword, Tool.toJavaList(userObj.getPermissions()));
                }
                return ResponseEntity.status(HttpStatus.OK).body(null);
            } else {
                error.setReason(RSSErrorReason.INTERNAL_ERROR);
                resBody.put("error", error.getRSSError());
                log.info(String.format("[Patch] %s / DB communication error.", request.getServletPath()));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);
            }
        } else {
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            log.info(String.format("[Patch] %s / There are no users in the DB.", request.getServletPath()));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }
    }

    @PatchMapping("/{userID}/permission")
    @ResponseBody
    public ResponseEntity<?> changeAuth(HttpServletRequest request,
                                        @PathVariable("userID") String userID,
                                        @RequestBody Map<String, Object> param) throws Exception {
        log.info(String.format("[Patch] %s", request.getServletPath()));
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        if (userID == null || userID.equals("")) {
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        int id = Integer.parseInt(userID);
        List<String> permission = param.containsKey("permission")?(List<String>) param.get("permission"):null;

        if (permission == null) {
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
        }

        UserVo userObj = serviceUser.getUser(id);
        if (userObj != null && (userObj.getId() >= 10000)) {
            userObj.setPermissions(Tool.toCSVString(permission));
            if (serviceUser.modifyUser(userObj)) {
                if(Tool.isSystemUser(permission)) {
                    serviceManagerController.editUserToFile(userObj.getUsername(), userObj.getPassword(), permission);
                } else {
                    serviceManagerController.deleteUserToFile(userObj.getUsername());
                }
                resBody.put("permission", permission);
                return ResponseEntity.status(HttpStatus.OK).body(resBody);
            } else {
                error.setReason(RSSErrorReason.INTERNAL_ERROR);
                resBody.put("error", error.getRSSError());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);
            }
        } else {
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> create(HttpServletRequest request,
                                    @RequestBody Map<String, Object> param) throws Exception {
        log.info(String.format("[Post] %s", request.getServletPath()));
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        String userName = param.containsKey("userName") ? (String) param.get("userName") : null;
        String password = param.containsKey("password") ? (String) param.get("password") : null;
        List<String> permission = param.containsKey("permission")?(List<String>) param.get("permission"):null;

        if (userName == null || password == null || permission == null
                || userName.isEmpty() || password.isEmpty()) {
            error.setReason(RSSErrorReason.INVALID_PARAMETER);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resBody);
        }

        UserVo userObj = serviceUser.getUser(userName);
        if (userObj != null) {
            error.setReason(RSSErrorReason.DUPLICATE_USER);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(resBody);
        }

        UserVo newUserObj = new UserVo();
        newUserObj.setUsername(userName);
        newUserObj.setPassword(password);
        newUserObj.setPermissions(Tool.toCSVString(permission));

        if (!serviceUser.addUser(newUserObj)) {
            error.setReason(RSSErrorReason.INTERNAL_ERROR);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);

        }

        if(Tool.isSystemUser(permission)) {
            serviceManagerController.addUserToFile(userName, password, permission);
        }

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @DeleteMapping("/{userId}")
    @ResponseBody
    public ResponseEntity<?> deleteUser(HttpServletRequest request,
                                        @PathVariable("userId") String userId) throws Exception {

        log.info(String.format("[Delete] %s", request.getServletPath()));
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        if (userId == null || userId.isEmpty()) {
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        int id = Integer.parseInt(userId);

        UserVo userObj = serviceUser.getUser(id);
        if (userObj == null) {
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }

        if (!serviceUser.deleteUser(userObj)) {
            error.setReason(RSSErrorReason.INTERNAL_ERROR);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resBody);

        }

        if(Tool.isSystemUser(Tool.toJavaList(userObj.getPermissions()))) {
            serviceManagerController.deleteUserToFile(userObj.getUsername());
        }

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<?> loadUserList(HttpServletRequest request) throws Exception {
        log.info(String.format("[Get] %s", request.getServletPath()));
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        /*
        try {
            int permission = Integer.parseInt(jwtService.getCurAccTokenUserPermission());
            if(permission < ADMIN_ACCOUNT) {
                error.setReason(RSSErrorReason.INSUFFICIENT_PERMISSIONS);
                resBody.put("error", error.getRSSError());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resBody);
            }
        } catch (Exception e) {
            error.setReason(RSSErrorReason.NOT_FOUND);
            resBody.put("error", error.getRSSError());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resBody);
        }
        */

        List<UserVo> lists = serviceUser.getUserList();
        List<RSSUserResponse> userList = new ArrayList<RSSUserResponse>();
        for (UserVo list : lists) {
            RSSUserResponse user = new RSSUserResponse();
            SimpleDateFormat conTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            user.setUserId(list.getId());
            user.setUserName(list.getUsername());
            user.setPermission(Tool.toJavaList(list.getPermissions()));
            user.setCreated(list.getCreated() != null ? conTimeFormat.format(list.getCreated()) : null);
            user.setModified(list.getModified() != null ? conTimeFormat.format(list.getModified()) : null);
            user.setLastAccess(list.getLastAccess() != null ? conTimeFormat.format(list.getLastAccess()) : null);
            userList.add(user);
        }

        resBody.put("lists", userList);
        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }

    @GetMapping("/total")
    @ResponseBody
    public ResponseEntity<?> getTotalCnt(HttpServletRequest request) throws Exception {
        log.info(String.format("[Get] %s", request.getServletPath()));
        Map<String, Object> resBody = new HashMap<>();
        RSSError error = new RSSError();

        int cnt = serviceUser.getUserTotalCnt();

        if(cnt<0){
            resBody.put("error", error.getRSSError());
        }
        else
        {
            resBody.put("cnt", cnt);
        }
        return ResponseEntity.status(HttpStatus.OK).body(resBody);
    }
}

