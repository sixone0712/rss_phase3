package jp.co.canon.cks.eec.fs.rssportal.controller;

import jp.co.canon.cks.eec.fs.rssportal.service.CommandService;
import jp.co.canon.cks.eec.fs.rssportal.vo.CommandVo;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class CmdControllerTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    public CmdControllerTest() {
    }

    @Test
    void getCmdListTest() throws Exception {


        MultiValueMap<String, String> info = new LinkedMultiValueMap<>();
        String url = "/api/auths/login";
        info.add("username", "ymkwon");
        info.add("password", "46f94c8de14fb36680850768ff1b7f2a");
        // when
        MvcResult result= mockMvc.perform(get(url).params(info).servletPath(url))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        JSONObject content = new JSONObject(result.getResponse().getContentAsString());
        log.info("content : "+content);
        log.info("accessToken : "+content.getString("accessToken"));

        String url2 = "/api/vftp/command";
        mockMvc.perform(get(url2).servletPath(url2).param("type","vftp_sss").header("Authorization", "Bearer " + content.getString("accessToken")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());

        mockMvc.perform(get(url2).servletPath(url2).param("type","vftp_compat").header("Authorization", "Bearer " + content.getString("accessToken")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());

        mockMvc.perform(get(url2).servletPath(url2).header("Authorization", "Bearer " + content.getString("accessToken")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());

        mockMvc.perform(get(url2).param("type","300").servletPath(url2).header("Authorization", "Bearer " + content.getString("accessToken")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test   /*test 1 - in case : Type is normal   name normal*/
    void add_delete_CmdTest() throws Exception {

        MultiValueMap<String, String> info = new LinkedMultiValueMap<>();
        String url = "/api/auths/login";
        info.add("username", "ymkwon");
        info.add("password", "46f94c8de14fb36680850768ff1b7f2a");
        // when

        MvcResult result= mockMvc.perform(get(url).params(info).servletPath(url))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        JSONObject content = new JSONObject(result.getResponse().getContentAsString());
        log.info("content : "+content);
        log.info("accessToken : "+content.getString("accessToken"));

        String url2 = "/api/vftp/command";

        MultiValueMap<String, String> temp = new LinkedMultiValueMap<>();
        JSONObject object = new JSONObject();
        object.put("cmd_type","");

        mockMvc.perform(post(url2)
                .servletPath(url2)
                .header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        object.remove("cmd_type");

        object.put("cmd_type","vftp_111");
        object.put("cmd_name","unit_test_cmd");
        mockMvc.perform(post(url2)
                .servletPath(url2)
                .header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());


        object.remove("cmd_type");
        object.remove("cmd_name");

        object.put("cmd_type","vftp_sss");
        object.put("cmd_name","unit_test_cmd");
        MvcResult result2 = mockMvc.perform(post(url2)
                .servletPath(url2)
                .header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        JSONObject content2 = new JSONObject(result2.getResponse().getContentAsString());
        log.info("content2 : "+content2);
        log.info("id : "+content2.get("id"));

        //Duplicated Command
        mockMvc.perform(post(url2)
                .servletPath(url2)
                .header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());

        String deleteUrl = "/api/vftp/command/" +content2.get("id");
        /*modify*/
        /*1. param  is null*/
        mockMvc.perform(put(deleteUrl)
                .servletPath(deleteUrl)
                .header("Authorization", "Bearer " + content.getString("accessToken")))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());

        object.remove("cmd_name");

        /*2. "cmd_name" param is empty*/
        mockMvc.perform(put(deleteUrl)
                .servletPath(deleteUrl)
                .header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());

        object.put("cmd_name","unit_test_cmd_modify");

        /*3. normal - OK*/
        mockMvc.perform(put(deleteUrl)
                .servletPath(deleteUrl)
                .header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());


        /*delete*/
        mockMvc.perform(delete(deleteUrl)
                .servletPath(deleteUrl)
                .header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        //empty command
        mockMvc.perform(delete(deleteUrl)
                .servletPath(deleteUrl)
                .header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        mockMvc.perform(put(deleteUrl)
                .servletPath(deleteUrl)
                .header("Authorization", "Bearer " + content.getString("accessToken"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(object))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}