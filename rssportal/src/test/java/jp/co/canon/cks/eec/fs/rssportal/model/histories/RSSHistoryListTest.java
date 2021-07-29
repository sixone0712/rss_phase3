package jp.co.canon.cks.eec.fs.rssportal.model.histories;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RSSHistoryListTest {

    @Test
    void test() {
        RSSHistoryList history = new RSSHistoryList();
        final String smile = ":)";
        history.setHistoryId(10);
        assertEquals(10, history.getHistoryId());
        history.setType(smile);
        assertEquals(smile, history.getType());
        history.setDate(smile);
        assertEquals(smile, history.getDate());
        history.setFileName(smile);
        assertEquals(smile, history.getFileName());
        history.setUserName(smile);
        assertEquals(smile, history.getUserName());
        history.setStatus(smile);
        assertEquals(smile, history.getStatus());
    }
}