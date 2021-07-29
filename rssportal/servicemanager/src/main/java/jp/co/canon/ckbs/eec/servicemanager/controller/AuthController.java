package jp.co.canon.ckbs.eec.servicemanager.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import jp.co.canon.ckbs.eec.servicemanager.session.SessionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final HttpSession httpSession;

    @Value("${servicemanager.admin-init-password}")
    private String adminInitPassword;

    @Value("${servicemanager.admin-init-password-filename}")
    private String adminInitPasswordFilename;

    @Autowired
    public AuthController(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    @GetMapping("/me")
    @ResponseBody
    public ResponseEntity<?> isLogin(HttpServletRequest request)  throws Exception {
        if(httpSession.getAttribute("context") != null) {
            SessionContext context = (SessionContext)httpSession.getAttribute("context");
            if(context.isAuthenticated()) {
                Map<String, Object> resBody = new HashMap<>();
                resBody.put("username", context.getUsername());
                resBody.put("permission", context.getPermission());
                return ResponseEntity.status(HttpStatus.OK).body(resBody);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping("/login")
    @ResponseBody
    public ResponseEntity<?> Login(HttpServletRequest request,
        @RequestParam(name="username", required = false, defaultValue = "") String username,
        @RequestParam(name="password", required = false, defaultValue = "") String password)  throws Exception {

        if(username == null || username.equals("") || password == null || password.equals("")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        try {
            Gson gsonRead = new Gson();
            JsonReader jsonReader = new JsonReader(new FileReader(adminInitPasswordFilename));
            List<UserInfoFile> list = gsonRead.fromJson(jsonReader, new TypeToken<List<UserInfoFile>>(){}.getType());

            for(UserInfoFile item : list) {
                if(item.getUsername().equals(username) && item.getPassword().equals(password)) {
                    Map<String, Object> resBody = new HashMap<>();
                    resBody.put("username", item.getUsername());
                    resBody.put("permission", item.getPermission());
                    SessionContext context = new SessionContext();
                    context.setAuthenticated(true);
                    context.setUsername(item.getUsername());
                    context.setPermission(item.getPermission());
                    httpSession.setAttribute("context", context);

                    return ResponseEntity.status(HttpStatus.OK).body(resBody);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    @GetMapping("/logout")
    @ResponseBody
    public ResponseEntity<?> Login(HttpServletRequest request)  throws Exception {
        this.httpSession.invalidate();
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
