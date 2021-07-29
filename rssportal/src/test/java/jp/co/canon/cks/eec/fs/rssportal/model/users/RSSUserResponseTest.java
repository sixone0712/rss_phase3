package jp.co.canon.cks.eec.fs.rssportal.model.users;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RSSUserResponseTest {

    @Test
    void test() {
        RSSUserResponse response = new RSSUserResponse();
        final String smile = ":(";
        response.setUserId(10);
        response.setUserName(smile);
        response.setPermission(smile);
        response.setCreated(smile);
        response.setModified(smile);
        response.setLastAccess(smile);
        assertEquals(10, response.getUserId());
        assertEquals(smile, response.getUserName());
        assertEquals(smile, response.getPermission());
        assertEquals(smile, response.getCreated());
        assertEquals(smile, response.getModified());
        assertEquals(smile, response.getLastAccess());
    }
}