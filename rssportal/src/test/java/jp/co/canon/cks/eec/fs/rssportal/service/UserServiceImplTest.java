package jp.co.canon.cks.eec.fs.rssportal.service;

import jp.co.canon.cks.eec.fs.rssportal.vo.UserVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceImplTest {

    @Autowired
    private UserService service;

    @Test
    void test() {
        assertNotNull(service);

        assertNotNull(service.getUserList());

        UserVo vo = new UserVo();
        vo.setUsername("");
        vo.setPassword("");
        assertFalse(service.addUser(vo));
        vo.setId(999);
        assertFalse(service.modifyUser(vo));
        assertFalse(service.deleteUser(vo));
        assertFalse(service.updateRefreshToken(999, ":)"));
        assertTrue(service.cleanBlacklist(new Date()));
    }

}