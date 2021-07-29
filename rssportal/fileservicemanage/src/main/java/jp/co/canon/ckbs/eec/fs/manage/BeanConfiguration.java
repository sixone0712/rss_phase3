package jp.co.canon.ckbs.eec.fs.manage;

import jp.co.canon.ckbs.eec.fs.collect.DefaultFileServiceCollectConnectorFactory;
import jp.co.canon.ckbs.eec.fs.collect.FileServiceCollectConnectorFactory;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.ConfigurationService;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.ConfigurationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {
    @Value("${fileservice.structureFile}")
    String structureFileName;

    @Value("${fileservice.categoriesFile}")
    String categoriesFileName;

    @Autowired
    ConfigurationService configurationService;

    @Bean
    public FileServiceCollectConnectorFactory getFileServiceCollectorFactory(){
        return new DefaultFileServiceCollectConnectorFactory(configurationService);
    }

    @Bean
    public ConfigurationService getConfigurationService(){
        return new ConfigurationServiceImpl(structureFileName, categoriesFileName);
    }
}
