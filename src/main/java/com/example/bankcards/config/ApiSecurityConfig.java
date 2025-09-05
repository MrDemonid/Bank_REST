package com.example.bankcards.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Конфигурация сервера ресурсов, реализующего REST API.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AutoConfigureAfter(AuthSecurityConfig.class)
@Log4j2
public class ApiSecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // список разрешённых доменов
        configuration.setAllowedOrigins(List.of(
                "http://localhost:9000",    // фронтенд dev
                "https://example.bank.com"  // фронтенд продакшн
        ));

        // разрешаем методы
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // разрешаем заголовки
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));

        // разрешаем отправку cookie/Authorization заголовка
        configuration.setAllowCredentials(true);

        // время кэширования preflight-ответа
        configuration.setMaxAge(3600L);

        // применяем конфигурацию ко всем эндроинтам
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Цепочка фильтров безопасности.
     */
    @Order(2)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Starting api security filter chain");
        http
                .securityMatcher("/api/**")
                .cors(Customizer.withDefaults()) // Разрешаем CORS
//                .csrf(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)                      // Отключаем CSRF для запросов API
                .authorizeHttpRequests(authorize -> authorize
                        // мониторинг
                        .requestMatchers("/api/cards/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .anyRequest().authenticated()              // Остальные требуют аутентификации
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jt -> jt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .httpBasic(AbstractHttpConfigurer::disable)   // Отключаем Basic Auth
                .formLogin(AbstractHttpConfigurer::disable);    // Отключаем перенаправление на форму входа

        return http.build();
    }

    /**
     * Фильтр для dev. Разрешает доступ к swagger и включает httpBasic аутентификацию, чтобы легко получить Jwt-токен для тестов.
     */
    @Order(3)
    @Profile("dev")
    @Bean
    public SecurityFilterChain swaggerSecurity(HttpSecurity http) throws Exception {
        log.info("Starting swagger filter chain");
        http
                .securityMatcher(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/swagger-resources",
                        "/webjars/**")
                .cors(Customizer.withDefaults())
// Если в dev нужен доступ с любого адреса, то просто убираем комментарии:
//                .cors(cors -> cors.configurationSource(request -> {
//                    CorsConfiguration config = new CorsConfiguration();
//                    config.addAllowedOriginPattern("*");                      // любой фронт
//                    config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
//                    config.setAllowedHeaders(List.of("*"));
//                    config.setAllowCredentials(true);
//                    return config;
//                }))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /**
     * Извлекает из полей запроса значения ROLE.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new CustomJwtAuthenticationConverter());
        return converter;
    }

}
