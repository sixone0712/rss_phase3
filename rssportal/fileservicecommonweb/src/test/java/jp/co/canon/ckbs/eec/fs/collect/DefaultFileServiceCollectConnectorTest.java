package jp.co.canon.ckbs.eec.fs.collect;

import jp.co.canon.ckbs.eec.fs.manage.service.configuration.ConfigurationService;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.ConfigurationServiceImpl;
import org.junit.jupiter.api.Test;

public class DefaultFileServiceCollectConnectorTest {

    @Test
    void test_001(){
        ConfigurationService configurationService = new ConfigurationServiceImpl("/CANON/ENV/structure.json", "/CANON/ENV/categories.json");
        DefaultFileServiceCollectConnectorFactory factory = new DefaultFileServiceCollectConnectorFactory(configurationService);
        FileServiceCollectConnector connector = factory.getConnector("192.168.222.211");
        connector.cancelAndDeleteRequest("MPA_1", "ABCDEFG");
    }
}
