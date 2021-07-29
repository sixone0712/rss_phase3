package jp.co.canon.cks.eec.fs.rssportal.dao;

import jp.co.canon.cks.eec.fs.rssportal.vo.CommandVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CommandDaoImplTest {

    @Autowired
    private CommandDao dao;

    @Test
    void test() {
        assertNotNull(dao);
        assertNotNull(dao.findAll());
        Map<String, Object> param = new HashMap<>();
        assertNull(dao.findCommandList(param));
        assertNull(dao.find(param));
        param.put("id", 1);
        dao.find(param);
        CommandVo vo = new CommandVo();
        vo.setCmd_name(":)");
        vo.setCmd_type(":)");
        vo.setCreated(new Date());
        vo.setModified(new Date());
        assertTrue(dao.add(vo)>0);
        assertTrue(dao.modify(new CommandVo()));
        assertTrue(dao.delete(new CommandVo()));
    }

}