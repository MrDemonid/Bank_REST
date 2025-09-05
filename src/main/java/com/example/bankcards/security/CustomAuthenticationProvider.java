package com.example.bankcards.security;

import com.example.bankcards.entity.auth.User;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


/**
 * Провайдер аутентификации.
 *
 * Просто подменяет UserDetailsService и PasswordEncoder на наши реализации,
 * чтобы работать с нашей БД и своими структурами хранения пользователей.
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;


    public CustomAuthenticationProvider(UserDetailsServiceJpa userDetailsService, CustomPasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        // загружаем пользователя через UserDetailsService
        User user = (User) userDetailsService.loadUserByUsername(username);

        // проверяем пароль
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Неверный пароль") {};    // это абстрактное исключение, поэтому создаем ему реализацию {}
        }

        // возвращаем успешно аутентифицированный объект Authentication
        return new UsernamePasswordAuthenticationToken(username, password, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
