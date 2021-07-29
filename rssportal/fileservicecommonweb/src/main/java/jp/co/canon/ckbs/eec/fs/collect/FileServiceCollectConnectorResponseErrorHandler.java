package jp.co.canon.ckbs.eec.fs.collect;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

public class FileServiceCollectConnectorResponseErrorHandler extends DefaultResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        int rawStatusCode = response.getRawStatusCode();
        if (rawStatusCode == 502){
            return false;
        }
        return super.hasError(response);
    }
}
