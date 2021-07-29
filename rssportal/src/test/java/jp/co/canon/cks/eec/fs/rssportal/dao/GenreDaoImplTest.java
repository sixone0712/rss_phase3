package jp.co.canon.cks.eec.fs.rssportal.dao;

import jp.co.canon.cks.eec.fs.rssportal.vo.GenreVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GenreDaoImplTest {

    @Autowired
    private GenreDao d;

    @Test
    void test() {
        assertNotNull(d);

        assertNotNull(d.findAll());
        Map<String, Object> map = new HashMap<>();
        assertNull(d.findById(map));
        map.put("id", 10101010);
        assertNull(d.findById(map));
        GenreVo v = new GenreVo();
        v.setName(":)");
        v.setCategory(":)");
        v.setCreated(new Date());
        v.setModified(new Date());
        assertTrue(d.add(v));
        assertTrue(d.modify(new GenreVo()));
        map.put("name", ":)");
        GenreVo vo = d.findByName(map);
        assertNotNull(vo);
        map.remove("id");
        map.put("id", vo.getId());
        assertTrue(d.delete(map));
        assertTrue(d.modifyUpdate());
        assertNotNull(d.findUpdate());
    }
}