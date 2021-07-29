package jp.co.canon.ckbs.eec.fs.collect.service.configuration;

import jp.co.canon.ckbs.eec.fs.manage.service.configuration.Machine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MachineTest {
    @Test
    void test_001(){
        Machine machine = new Machine("MPA_1", "10.1.36.118", "ots1", "Fab2", "fuser", "fpass", "vuser", "vpass", "sn", "tt", 81);
        Assertions.assertEquals("MPA_1", machine.getMachineName());
    }

    @Test
    void test_002(){
        Machine machine = new Machine();
        machine.setMachineName("MPA_2");
        Assertions.assertEquals("MPA_2", machine.getMachineName());
    }
}
