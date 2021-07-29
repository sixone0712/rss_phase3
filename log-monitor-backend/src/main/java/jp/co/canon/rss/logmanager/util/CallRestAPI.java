package jp.co.canon.rss.logmanager.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public class CallRestAPI {
    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

    public ResponseEntity<?> getRestAPI(String url, Class<?> responseType) {
        ResponseEntity<?> response = null;
        try {
            factory.setReadTimeout(5000); // 읽기시간초과, ms
            factory.setConnectTimeout(3000); // 연결시간초과, ms
            HttpClient httpClient = HttpClientBuilder.create()
                    .setMaxConnTotal(100) // connection pool 적용
                    .setMaxConnPerRoute(5) // connection pool 적용
                    .build();
            factory.setHttpClient(httpClient); // 동기실행에 사용될 HttpClient 세팅

            RestTemplate restTemplate = new RestTemplate(factory);

            response = restTemplate.getForEntity(url, responseType);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return response;
    }
}
