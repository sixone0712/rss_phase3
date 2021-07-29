package jp.co.canon.cks.eec.fs.rssportal.controller;

import io.jsonwebtoken.Jwt;
import jp.co.canon.cks.eec.fs.rssportal.session.SessionContext;
import jp.co.canon.cks.eec.fs.rssportal.vo.UserVo;
//import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;   //compile Error
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UserController userController;
    private final String USER_NAME = "name";
    private final String USER_AUTH = "auth";

    @Autowired
    private MockMvc mockMvc;
    private MockHttpServletRequest request;

    @Autowired
    public UserControllerTest(UserController userController) {
        this.userController = userController;
    }


    @Test
    void UserController_Test() throws Exception {
        MultiValueMap<String, String> info = new LinkedMultiValueMap<>();
        String url = "/api/auths/login";

        info.add("username", "ymkwon");
        info.add("password", "46f94c8de14fb36680850768ff1b7f2a");
        // when
        MvcResult result = mockMvc.perform(get(url).params(info).servletPath(url))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        JSONObject content = new JSONObject(result.getResponse().getContentAsString());
        log.info("userId : " + content.get("userId"));

        /*[Administrator] LoadUserList*/
        String url2 = "/api/users";
        mockMvc.perform(get(url2).servletPath(url2).header("Authorization", "Bearer " + content.getString("accessToken")))
                .andExpect(MockMvcResultMatchers.status().isOk());

        /*[Administrator] CreateUser*/
        JSONObject object = new JSONObject();
        object.put("userName","Administrator");

        mockMvc.perform(post(url2)
                .params(info)
                .servletPath(url2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + content.getString("accessToken"))
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());

        object.put("password","46f94c8de14fb36680850768ff1b7f2a");
        object.put("permission","50");
        mockMvc.perform(post(url2)
                .params(info)
                .servletPath(url2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + content.getString("accessToken"))
        )
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andDo(MockMvcResultHandlers.print());

        object.remove("userName");
        object.put("userName","Test_user");
                mockMvc.perform(post(url2)
                .params(info)
                .servletPath(url2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + content.getString("accessToken")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());

        UserController_UserTest1();
        UserController_UserTest2();
        UserController_UserTest3();

    }
    void UserController_UserTest1() throws Exception {
        MultiValueMap<String, String> info = new LinkedMultiValueMap<>();
        String url = "/api/auths/login";

        info.add("username", "Test_user");
        info.add("password", "46f94c8de14fb36680850768ff1b7f2a");

        // when
        MvcResult result = mockMvc.perform(get(url).params(info).servletPath(url))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        JSONObject content = new JSONObject(result.getResponse().getContentAsString());
        log.info("userId : " + content.get("userId"));

        /*[GeneralUser] LoadUserList*/
        String url2 = "/api/users";
        mockMvc.perform(get(url2).servletPath(url2).header("Authorization", "Bearer " + content.getString("accessToken")))
                .andExpect(MockMvcResultMatchers.status().isForbidden());

        JSONObject object = new JSONObject();

        /*[GeneralUser] Permission Modify*/
        String url3= "/api/users/"+content.get("userId")+"/permission";

        /*[GeneralUser] Permission not setting*/
        mockMvc.perform(patch(url3).servletPath(url3).header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        /*[GeneralUser] invalid Permission*/
        object.put("permission","300");
        mockMvc.perform(patch(url3).servletPath(url3).header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        /*[GeneralUser] normal Permission*/
        object.remove("permission");
        object.put("permission","10");
        mockMvc.perform(patch(url3).servletPath(url3).header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(patch("/api/users/1/permission").servletPath("/api/users/1/permission").header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    void UserController_UserTest2() throws Exception {
        MultiValueMap<String, String> info = new LinkedMultiValueMap<>();
        String url = "/api/auths/login";

        info.add("username", "Test_user");
        info.add("password", "46f94c8de14fb36680850768ff1b7f2a");

        // when
        MvcResult result = mockMvc.perform(get(url).params(info).servletPath(url))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        JSONObject content = new JSONObject(result.getResponse().getContentAsString());
        log.info("userId : " + content.get("userId"));

        JSONObject object = new JSONObject();

        /*[GeneralUser] Permission Modify*/
        String passwordUrl= "/api/users/"+content.get("userId")+"/password";

        /*[GeneralUser] password not setting*/
        object.remove("permission");
        mockMvc.perform(patch(passwordUrl).servletPath(passwordUrl).header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        /*[GeneralUser] invalid parameter*/
        object.put("oldPassword","46f94c8de14fb36680850768ff1b7f2a");
        mockMvc.perform(patch(passwordUrl).servletPath(passwordUrl).header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        /*[GeneralUser] normal Permission*/
        object.remove("oldPassword");
        object.put("oldPassword","46f94c8de14fb36680850768ff1b7f2a");
        object.put("newPassword","1");
        mockMvc.perform(patch(passwordUrl).servletPath(passwordUrl).header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(patch(passwordUrl).servletPath(passwordUrl).header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        object.remove("oldPassword");
        object.remove("newPassword");
        object.put("oldPassword","1");
        object.put("newPassword","46f94c8de14fb36680850768ff1b7f2a");
        mockMvc.perform(patch(passwordUrl).servletPath(passwordUrl).header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(patch("/api/users/1/password").servletPath("/api/users/1/password").header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }
    void UserController_UserTest3() throws Exception {
        MultiValueMap<String, String> info = new LinkedMultiValueMap<>();
        String url = "/api/auths/login";

        info.add("username", "Test_user");
        info.add("password", "46f94c8de14fb36680850768ff1b7f2a");

        // when
        MvcResult result = mockMvc.perform(get(url).params(info).servletPath(url))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        JSONObject content = new JSONObject(result.getResponse().getContentAsString());
        log.info("userId : " + content.get("userId"));

        JSONObject object = new JSONObject();

        String deleteUrl= "/api/users/"+content.get("userId");

        mockMvc.perform(delete(deleteUrl)
                .servletPath(deleteUrl)
                .header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(delete("/api/users/1")
                .servletPath("/api/users/1")
                .header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }
}