package jp.co.canon.cks.eec.fs.rssportal.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtServiceImplTest {

    @Autowired
    private JwtService service;

    @Test
    void test() {
        assertNotNull(service);

        assertNull(service.create(null, ":)"));
        assertNull(service.create(":)", ":)"));
        assertNotNull(service.create(new HashMap<>(), "refreshToken"));
    }

}