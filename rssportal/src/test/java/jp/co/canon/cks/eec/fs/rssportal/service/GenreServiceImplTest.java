package jp.co.canon.cks.eec.fs.rssportal.service;

import jp.co.canon.cks.eec.fs.rssportal.vo.GenreVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GenreServiceImplTest {

    @Autowired
    private GenreService service;

    @Test
    void test() {
        assertNotNull(service);
        assertTrue(service.addGenreUpdate());
        GenreVo vo = new GenreVo();
        vo.setId(10000000);
        assertFalse(service.modifyGenre(vo));
    }

}