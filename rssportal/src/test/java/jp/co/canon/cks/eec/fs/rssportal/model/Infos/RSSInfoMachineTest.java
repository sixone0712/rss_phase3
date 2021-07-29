package jp.co.canon.cks.eec.fs.rssportal.model.Infos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RSSInfoMachineTest {

    @Test
    void test() {
        RSSInfoMachine info = new RSSInfoMachine();
        info.setFabName(":)");
        info.setMachineName(":)");
        assertEquals(":)", info.getFabName());
        assertEquals(":)", info.getMachineName());
    }
}