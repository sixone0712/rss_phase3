package jp.co.canon.ckbs.eec.fs.collect;

import jp.co.canon.ckbs.eec.fs.manage.service.configuration.ConfigurationService;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class DefaultFileServiceCollectConnectorFactory implements FileServiceCollectConnectorFactory{
    RestTemplate restTemplate;
    ConfigurationService configurationService;

    public DefaultFileServiceCollectConnectorFactory(ConfigurationService configurationService){
        this.configurationService = configurationService;
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(10000);
        HttpClient httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(500)
                .setMaxConnPerRoute(100)
                .build();
        factory.setHttpClient(httpClient);
        restTemplate = new RestTemplate(factory);
        restTemplate.setErrorHandler(new FileServiceCollectConnectorResponseErrorHandler());
    }

    @Override
    public FileServiceCollectConnector getConnector(String host) {
        return new DefaultFileServiceCollectConnector(host, this.restTemplate, this.configurationService);
    }
}
