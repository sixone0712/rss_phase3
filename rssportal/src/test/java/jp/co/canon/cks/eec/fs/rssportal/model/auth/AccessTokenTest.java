package jp.co.canon.cks.eec.fs.rssportal.model.auth;

import jp.co.canon.cks.eec.fs.rssportal.Defines.RSSErrorReason;
import jp.co.canon.cks.eec.fs.rssportal.model.FileInfo;
import jp.co.canon.cks.eec.fs.rssportal.model.error.ExpiredException;
import jp.co.canon.cks.eec.fs.rssportal.model.error.RSSError;
import jp.co.canon.cks.eec.fs.rssportal.model.error.UnauthorizedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AccessTokenTest {
    @Test
    public void test(){

        ExpiredException test1 = new ExpiredException();
        UnauthorizedException test2 = new UnauthorizedException();

        RSSError errorTest = new RSSError();
        errorTest.setReason("Reason");
        errorTest.setMessage("Message");
        errorTest.toString();

        Map<String, Object> getReturn= null;
        getReturn = errorTest.getRSSError();
        Assertions.assertEquals("Message", getReturn.get("message"));
        Assertions.assertEquals("Reason", getReturn.get("reason"));

        accessToken_test();
        refresh_Token_test();

        RSSErrorReason errorreason = new RSSErrorReason();
        errorreason.toString();
    }
    public void accessToken_test(){

        AccessToken token = new AccessToken();
        token.setSub("TestToken");
        token.setExp(new Date());
        token.setPermission("100");
        token.setUserId(10000);
        token.setUserName("Administrator");
        token.setIat(new Date());
        token.toString();

        Assertions.assertEquals("Administrator", token.getUserName());
        Assertions.assertEquals(10000, token.getUserId());
        Assertions.assertEquals("100", token.getPermission());
        Assertions.assertEquals("TestToken", token.getSub());
        Assertions.assertNotNull(token.getExp());
        Assertions.assertNotNull(token.getIat());
    }
    public void refresh_Token_test(){

        RefreshToken token = new RefreshToken();
        token.setSub("TestToken");
        token.setExp(new Date());
        token.setUserId(10000);
        token.setUserName("Administrator");
        token.setIat(new Date());

        Assertions.assertEquals("Administrator", token.getUserName());
        Assertions.assertEquals(10000, token.getUserId());
        Assertions.assertEquals("TestToken", token.getSub());
        Assertions.assertNotNull(token.getExp());
        Assertions.assertNotNull(token.getIat());
    }
}