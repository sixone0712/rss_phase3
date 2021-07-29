package jp.co.canon.cks.eec.fs.rssportal.controller;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
class FileServiceControllerTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    public FileServiceControllerTest() {; }

    @Test
    void Test1() throws Exception {
        MultiValueMap<String, String> info = new LinkedMultiValueMap<>();
        String url = "/api/auths/login";

        info.add("username", "Administrator");
        info.add("password", "5f4dcc3b5aa765d61d8327deb882cf99");
        // when
        MvcResult result = mockMvc.perform(get(url).params(info).servletPath(url))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        JSONObject content = new JSONObject(result.getResponse().getContentAsString());
        log.info("userId : " + content.get("userId"));

        /*[Administrator] LoadUserList*/
        String url2 = "/api/infos/machines";
        mockMvc.perform(get(url2).servletPath(url2).header("Authorization", "Bearer " + content.getString("accessToken")))
                .andExpect(MockMvcResultMatchers.status().isOk());

        String url3= "/api/infos/categories";
        mockMvc.perform(get(url3).servletPath(url3).header("Authorization", "Bearer " + content.getString("accessToken")))
                .andExpect(MockMvcResultMatchers.status().isOk());

        String url4= url3+"/MPA_1";
        mockMvc.perform(get(url4).servletPath(url4)
                .header("Authorization", "Bearer " + content.getString("accessToken")))
                .andExpect(MockMvcResultMatchers.status().isOk());

        String url5= url3+"/MPA_111";
        mockMvc.perform(get(url5).servletPath(url5)
                .header("Authorization", "Bearer " + content.getString("accessToken")))
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

}