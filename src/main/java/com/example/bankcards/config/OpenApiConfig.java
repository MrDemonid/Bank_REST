package com.example.bankcards.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


/**
 * Добавляем в Swagger UI кнопку "Authorize", чтобы иметь возможность выполнять
 * запросы к приложению прямо из UI.
 */
@Configuration
@Profile("dev")
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("oauth2", new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .flows(new OAuthFlows()
                                // входе по CLIENT_CREDENTIALS
                                .clientCredentials(new OAuthFlow()
                                        .tokenUrl("http://localhost:8080/oauth2/token")
                                        .scopes(new Scopes()
                                                .addString("read", "Read access")
                                                .addString("write", "Write access"))))))
                .addSecurityItem(new SecurityRequirement().addList("oauth2"));
    }
}
