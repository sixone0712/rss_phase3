package jp.co.canon.cks.eec.fs.rssportal.controller;

import jp.co.canon.cks.eec.fs.rssportal.service.UserService;
import jp.co.canon.cks.eec.fs.rssportal.service.UserServiceImpl;
import jp.co.canon.cks.eec.fs.rssportal.session.SessionContext;
import jp.co.canon.cks.eec.fs.rssportal.vo.UserVo;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DlHistoryControllerTest {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final DlHistoryController dlHistoryController;
    private final String HISTORY_RESULT = "result";
    private final String HISTORY_DATA = "data";

    @Autowired
    private MockMvc mockMvc;
    private MockHttpSession session;
    private SessionContext context;

    @Autowired
    public  DlHistoryControllerTest(DlHistoryController dlHistoryController) {
        this.dlHistoryController = dlHistoryController;
    }
    @BeforeEach
    void setUp() throws Exception {
        log.info("setup===============================");
        if(session==null) {
            log.info("create session");
            String user = "ymkwon";
            String pass = "c4ca4238a0b923820dcc509a6f75849b";
            Map<String, Object> res = new HashMap<>();
            UserVo LoginUser = new UserVo();
            LoginUser.setUsername(user);
            LoginUser.setPassword(pass);
            LoginUser.setPermissions("100");
            session = new MockHttpSession();
            SessionContext sessionContext = new SessionContext();
            sessionContext.setUser(LoginUser);
            sessionContext.setAuthorized(true);
            session.setAttribute("context",sessionContext);
        }
        log.info("setup===============================End");
    }

    @Test
    void DlHistoryTest() throws Exception {
        boolean resp = false;
        Map<String, Object> EmptyResp = null;

        MultiValueMap<String, String> info = new LinkedMultiValueMap<>();
        String url = "/api/auths/login";
        info.clear();
        info.add("username", "Administrator");
        info.add("password", "5f4dcc3b5aa765d61d8327deb882cf99");

        MvcResult result= mockMvc.perform(get(url).params(info).servletPath(url))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        JSONObject content = new JSONObject(result.getResponse().getContentAsString());
        log.info("content : "+content);
        log.info("accessToken : "+content.getString("accessToken"));

        String getHistoryList = "/api/histories/downloads";

        mockMvc.perform(get(getHistoryList).servletPath(getHistoryList).header("Authorization", "Bearer " + content.getString("accessToken")))
                .andExpect(MockMvcResultMatchers.status().isOk());

        JSONObject object = new JSONObject();
        object.put("type","test_type");

        mockMvc.perform(post(getHistoryList)
                .params(info)
                .servletPath(getHistoryList)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + content.getString("accessToken"))
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());

        object.put("filename","test_file");
        object.put("status","test");
        mockMvc.perform(post(getHistoryList)
                .params(info)
                .servletPath(getHistoryList)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + content.getString("accessToken"))
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());



//        /*test 1 - in case : list is null*/
//        EmptyResp = dlHistoryController.getHistoryList();
//        if(EmptyResp.get(HISTORY_RESULT) =="-1")
//        {
//            assertNull(EmptyResp.get(HISTORY_DATA));
//        }
//        else
//        {
//            assertEquals(0,EmptyResp.get(HISTORY_RESULT));
//            assertNotNull(EmptyResp.get(HISTORY_DATA));
//        }
//
//        /*test 2 - in case : param is null*/
//        resp = dlHistoryController.addDlHistory(null);
//        assertFalse(resp);
//
//        /*test 3 - in case : context is not null */
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        Field sessionField = DlHistoryController.class.getDeclaredField("httpSession");
//        sessionField.setAccessible(true);
//        sessionField.set(dlHistoryController, session);
//
//        Map<String, Object> param = new HashMap<>();
//        param.put("type",1);
//        param.put("filename","ymkwon_CR7_20200526_182447.zip");
//        param.put("status","User Cancel");
//
//
//        resp = dlHistoryController.addDlHistory(param);
//        assertTrue(resp);
//
//        /*test 4 - in case : context is null*/
//        session.setAttribute("context", null);
//        param.put("type",1);
//        param.put("filename","ymkwon_CR7_20200526_182447.zip");
//        param.put("status","not login");
//
//        resp = dlHistoryController.addDlHistory(param);
//        assertFalse(resp);
//
//        /*test 5 - in case : are somethings history */
//        Map<String, Object> getListResp = null;
//        getListResp = dlHistoryController.getHistoryList();
//        assertEquals(0, getListResp.get(HISTORY_RESULT));
//        assertNotNull(getListResp.get(HISTORY_DATA));
    }

    @Test
    void export() {
        log.info("export");
        MockHttpServletRequest _request = new MockHttpServletRequest();
        _request.setServerName("export");
        dlHistoryController.exportDownloadHistory(_request, new MockHttpServletResponse());
    }

}