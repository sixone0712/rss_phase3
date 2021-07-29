package jp.co.canon.rss.logmanager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class OpenApiConfig {
		@Bean
		public OpenAPI openAPI(@Value("${springdoc.version}") String appVersion) {
			Info info = new Info().title("Log Monitor").version(appVersion)
					.description("List of Rest APIs used by Log Monitor");
					/*.contact(new Contact().name("CKBS").email("test@test.com"));*/

			return new OpenAPI()
					.components(new Components())
					.info(info);
		}
}