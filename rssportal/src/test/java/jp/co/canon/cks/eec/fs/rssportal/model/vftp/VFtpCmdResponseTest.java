package jp.co.canon.cks.eec.fs.rssportal.model.vftp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VFtpCmdResponseTest {

    @Test
    void test() {
        VFtpCmdResponse response = new VFtpCmdResponse();
        final String smile = ":(";
        response.setId(10);
        response.setCmd_name(smile);
        response.setCmd_type(smile);
        response.setCreated(smile);
        response.setModified(smile);
        assertEquals(10, response.getId());
        assertEquals(smile, response.getCmd_name());
        assertEquals(smile, response.getCmd_type());
        assertEquals(smile, response.getCreated());
        assertEquals(smile, response.getModified());
    }

}