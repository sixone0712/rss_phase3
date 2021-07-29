package jp.co.canon.cks.eec.fs.rssportal.model.Infos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RSSInfoCategoryTest {

    @Test
    void test() {
        RSSInfoCategory info = new RSSInfoCategory();
        info.setCategoryCode(":)");
        assertEquals(":)", info.getCategoryCode());
        info.setCategoryName(":)");
        assertEquals(":)", info.getCategoryName());
    }

}