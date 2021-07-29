package jp.co.canon.cks.eec.fs.rssportal.dao;

import jp.co.canon.cks.eec.fs.rssportal.vo.UserVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserDaoImplTest {

    @Autowired
    private UserDao d;

    @Test
    void test() {
        assertNotNull(d);

        assertNotNull(d.findAll());
        assertNull(d.find(new HashMap()));

        UserVo user = new UserVo();
        user.setUsername(":)");
        user.setPassword(":)");

        assertTrue(d.add(user));
        assertTrue(d.modify(new UserVo()));

        List<UserVo> list = d.findAll();
        for(UserVo u: list) {
            if(u.getUsername().equals(":)")) {
                assertTrue(d.delete(u));
            }
        }
    }
}