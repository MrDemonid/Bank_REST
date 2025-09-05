package com.example.bankcards.config;

import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CustomAuthenticationProvider;
import com.example.bankcards.security.CustomPasswordEncoder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Конфигурация для сервера авторизации.
 */
@Configuration
@EnableMethodSecurity
@Log4j2
public class AuthSecurityConfig {

    private final AuthorizationProperties properties;
    private final UserRepository userRepository;
    private final Environment environment;


    public AuthSecurityConfig(AuthorizationProperties properties, UserRepository userRepository, Environment environment) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.environment = environment;
    }

    /**
     * Включаем в цепочку security свои AuthenticationProvider + UserDetailService + PasswordEncoder
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, CustomAuthenticationProvider authenticationProvider) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }

    /**
     * Репозиторий для хранения зарегистрированных клиентов.
     *
     * @param jdbcTemplate объект подключения к БД.
     * @return репозиторий.
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    /**
     * Регистрируем (добавляем в БД) клиента по умолчанию. Позволяет клиентам входить через форму авторизации.
     */
    @Bean
    ApplicationRunner clientRunner(RegisteredClientRepository registeredClientRepository, CustomPasswordEncoder passwordEncoder) {
        return args -> {
            log.info("Registering clients: {}", properties.getAuthClients().keySet());
            for (var client: properties.getAuthClients().values()) {
                if (registeredClientRepository.findByClientId(client.getClientId()) == null) {
                    log.info("Registering client '{}': {}", client.getClientName(), client.getClientId());
                    RegisteredClient.Builder builder = RegisteredClient
                            .withId(UUID.randomUUID().toString())
                            .clientId(client.getClientId())
                            .clientSecret(passwordEncoder.encode(client.getClientSecret()));

                    client.getGrantTypes().forEach(g -> builder.authorizationGrantType(new AuthorizationGrantType(g)));
                    Optional.ofNullable(client.getRedirectUris()).orElse(Collections.emptyList()).forEach(builder::redirectUri);
                    client.getScopes().forEach(builder::scope);

                    builder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
                    builder.tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofMinutes(client.getExpirationTime()))
                            .build());
                    builder.clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(false)
                            .build());

                    registeredClientRepository.save(builder.build());
                }
            }
        };
    }

    /**
     * Зададим корневой URL нашего сервера.
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(properties.getIssuerUrl())
                .build();
    }

    /**
     * Конфигурация эндпоинтов Auth-сервера, поскольку у нас монолит и он совмещен с сервером ресурсов.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {
        log.info("Starting Auth security filter chain");
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())   // применяем только к /oauth2/*, иначе будет конфликт с другими SecurityFilterChain.
                .with(authorizationServerConfigurer, (configurer) -> {
                    // кастомизация, если нужна.
                })
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * Модифицируем Jwt-токен.
     * Добавляем в токен роли пользователя (будем хранить в поле "roles").
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            if (context.getAuthorizationGrantType().getValue().equals("client_credentials") && Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
                // только для dev-профиля
                context.getClaims().claim("roles", List.of("ROLE_ADMIN","ROLE_USER"));
                System.out.println("-- credentials role!");
            } else {

                String username = context.getPrincipal().getName();
                userRepository.findByUsername(username).ifPresent(user -> {

                    // добавляем в токен роли
                    List<String> roles = user.getRoles().stream()
                            .map(role -> role.getName().startsWith("ROLE_") ? role.getName() : "ROLE_" + role.getName())
                            .collect(Collectors.toList());
                    context.getClaims().claim("roles", roles);

                    // добавляем дополнительные данные о пользователе.
                    context.getClaims().claim("user_id", user.getId().toString());      // в поле "sub" есть имя пользователя
                    context.getClaims().claim("email", user.getEmail());
                });
            }
        };
    }

}
