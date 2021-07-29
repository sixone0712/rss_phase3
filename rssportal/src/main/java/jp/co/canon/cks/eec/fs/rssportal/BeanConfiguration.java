package jp.co.canon.cks.eec.fs.rssportal;

import jp.co.canon.ckbs.eec.fs.collect.DefaultFileServiceCollectConnectorFactory;
import jp.co.canon.ckbs.eec.fs.collect.FileServiceCollectConnectorFactory;
import jp.co.canon.ckbs.eec.fs.manage.DefaultFileServiceManageConnectorFactory;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnectorFactory;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.ConfigurationService;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.ConfigurationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

@Configuration
public class BeanConfiguration {

    @Value("${rssportal.configuration.path}")
    String configPath;

    @Value("${rssportal.configuration.structureFile}")
    String structureFileName;

    @Value("${rssportal.configuration.categoriesFile}")
    String categoriesFileName;

    @Autowired
    ConfigurationService configurationService;

    @Bean
    public FileServiceManageConnectorFactory getFileServiceManageConnectorFactory(){
        return new DefaultFileServiceManageConnectorFactory();
    }

    @Bean
    public FileServiceCollectConnectorFactory getFileServiceCollectConnectorFactory() {
        return new DefaultFileServiceCollectConnectorFactory(configurationService);
    }

    @Bean
    public ConfigurationService getConfigurationService(){
        String structure = Paths.get(configPath, structureFileName).toString();
        String category = Paths.get(configPath, categoriesFileName).toString();
        return new ConfigurationServiceImpl(structure, category);
    }
}
