package jp.co.canon.cks.eec.fs.rssportal.model.plans;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RSSPlanFileListTest {

    @Test
    void test() {
        RSSPlanFileList list = new RSSPlanFileList();
        final String smile = ":(";
        list.setPlanId(10);
        assertEquals(10, list.getPlanId());
        list.setPlanName(smile);
        assertEquals(smile, list.getPlanName());
        list.setFileId(20);
        assertEquals(20, list.getFileId());
        list.setCreated(smile);
        assertEquals(smile, list.getCreated());
        list.setStatus(smile);
        assertEquals(smile, list.getStatus());
        list.setDownloadUrl(smile);
        assertEquals(smile, list.getDownloadUrl());
    }
}