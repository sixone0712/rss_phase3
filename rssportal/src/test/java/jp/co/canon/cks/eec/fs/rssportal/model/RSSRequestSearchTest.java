package jp.co.canon.cks.eec.fs.rssportal.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RSSRequestSearchTest {
    @Test
    public void test_getset(){
        RSSRequestSearch s = new RSSRequestSearch();

        s.setStructId("structId");
        Assertions.assertEquals("structId", s.getStructId());

        s.setTargetName("targetName");
        Assertions.assertEquals("targetName", s.getTargetName());

        s.setTargetType("targetType");
        Assertions.assertEquals("targetType", s.getTargetType());

        s.setLogType(2);
        Assertions.assertEquals(2, s.getLogType());

        s.setLogCode("logCode");
        Assertions.assertEquals("logCode", s.getLogCode());

        s.setLogName("logName");
        Assertions.assertEquals("logName", s.getLogName());

        s.setStartDate("20200601");
        Assertions.assertEquals("20200601", s.getStartDate());

        s.setEndDate("20200601");
        Assertions.assertEquals("20200601", s.getEndDate());

        s.setKeyword("keyword");
        Assertions.assertEquals("keyword", s.getKeyword());

        s.setDir("dir");
        Assertions.assertEquals("dir", s.getDir());

        String str = s.toString();
        Assertions.assertNotNull(str);
    }

    @Test
    void test() {
        RSSRequestSearch s = new RSSRequestSearch();
        final String smile = ":(";
        s.setStructId(smile);
        s.setTargetName(smile);
        s.setTargetType(smile);
        s.setLogType(10);
        s.setLogCode(smile);
        s.setLogName(smile);
        s.setStartDate(smile);
        s.setEndDate(smile);
        s.setKeyword(smile);
        s.setDir(smile);
        RSSRequestSearch search = s.getClone();
        Assertions.assertNotNull(search);
        Assertions.assertEquals(smile, search.getStructId());
        Assertions.assertEquals(smile, search.getTargetName());
        Assertions.assertEquals(smile, search.getTargetType());
        Assertions.assertEquals(10, search.getLogType());
        Assertions.assertEquals(smile, search.getLogCode());
        Assertions.assertEquals(smile, search.getLogName());
        Assertions.assertEquals(smile, search.getStartDate());
        Assertions.assertEquals(smile, search.getEndDate());
        Assertions.assertEquals(smile, search.getKeyword());
        Assertions.assertEquals(smile, search.getDir());

    }
}