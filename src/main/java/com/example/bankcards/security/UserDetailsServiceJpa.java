package com.example.bankcards.security;

import com.example.bankcards.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


/**
 * Сервис для получения данных о пользователе из БД.
 * Заменяем дефолтный сервис, чтобы работать со своим форматом данных и своей БД.
 */
@Service
@AllArgsConstructor
public class UserDetailsServiceJpa implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Пользователь '%s' не найден!", username)));
    }
}
