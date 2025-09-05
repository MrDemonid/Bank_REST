package com.example.bankcards.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Извлекает из JWT-токена роли (поле "roles") для контекста безопасности.
 */
public class CustomJwtAuthenticationConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final JwtGrantedAuthoritiesConverter delegate = new JwtGrantedAuthoritiesConverter();

    private final String authoritiesClaimName;
    private final String authorityPrefix;


    public CustomJwtAuthenticationConverter() {
        this("roles", "ROLE_");
    }

    public CustomJwtAuthenticationConverter(String authoritiesClaimName, String authorityPrefix) {
        this.authoritiesClaimName = authoritiesClaimName;
        this.authorityPrefix = authorityPrefix;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        Object claims = source.getClaim(authoritiesClaimName);
        if (claims instanceof List<?> roles) {
            return roles.stream()
                    .filter(Objects::nonNull)
                    .filter(item -> item instanceof String)
                    .map(Object::toString)
                    .map(role -> role.startsWith(authorityPrefix) ? role : authorityPrefix + role)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        }
        return List.of();
    }
}
