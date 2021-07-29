package jp.co.canon.ckbs.eec.servicemanager.connector;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DefaultServiceManagerConnectorFactory implements ServiceManagerConnectorFactory{
    RestTemplate restTemplate;

    public DefaultServiceManagerConnectorFactory(){
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(10);
        HttpClient httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(500)
                .setMaxConnPerRoute(100)
                .build();
        factory.setHttpClient(httpClient);
        restTemplate = new RestTemplate(factory);
    }

    @Override
    public ServiceManagerConnector getConnector(String host, int port) {
        return new DefaultServiceManagerConnector(host, port, restTemplate);
    }
}
