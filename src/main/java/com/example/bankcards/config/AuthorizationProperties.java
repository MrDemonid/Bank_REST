package com.example.bankcards.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * В этом классе будем хранить подгружаемые из application.yml настройки.
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2")
public class AuthorizationProperties {
    private Map<String, ClientProperties> authClients;

    @Getter
    @Setter
    public static class ClientProperties {
        private String clientName;
        private String clientId;
        private String clientSecret;
        private List<String> redirectUris;
        private List<String> grantTypes;
        private List<String> scopes;
        private long expirationTime;
    }

    // адрес сервера авторизации
    private String issuerUrl;
}
