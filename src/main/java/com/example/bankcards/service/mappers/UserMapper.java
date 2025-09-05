package com.example.bankcards.service.mappers;

import com.example.bankcards.dto.PageDTO;
import com.example.bankcards.dto.SignUpRequest;
import com.example.bankcards.dto.UpdateUserRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.auth.Role;
import com.example.bankcards.entity.auth.User;
import com.example.bankcards.security.CustomPasswordEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Конвертер для User.
 */
@Component
@AllArgsConstructor
@Log4j2
public class UserMapper {

    private final CustomPasswordEncoder customPasswordEncoder;


    /**
     * Конвертирует User в DTO
     * @param user Сущность User
     * @return Объект класса UserResponse
     */
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );
    }

    /**
     * Создание новой сущности User.
     * @param user  Валидная структура данных нового пользователя.
     * @param roles Валидный список сущностей ролей.
     * @return Сущность User.
     */
    public User toSignUpUser(SignUpRequest user, Set<Role> roles) {
        return new User(
                null,
                user.getUserName(),
                customPasswordEncoder.encode(user.getPassword()),
                user.getEmail() == null ? "" : user.getEmail(),
                true,
                roles
        );
    }

    /**
     * Создание новой сущности User.
     * @param user  Валидная структура данных нового пользователя.
     * @param roles Валидный список сущностей ролей.
     * @return Сущность User.
     */
    public User toUpdateUser(UpdateUserRequest user, Set<Role> roles) {
        return new User(
                user.getId(),
                user.getUserName(),
                customPasswordEncoder.encode(user.getPassword()),
                user.getEmail() == null ? "" : user.getEmail(),
                user.isEnabled(),
                roles
        );
    }

    /**
     * Конвертация страничной выборки сущностей User в выборку DTO.
     * @param users Выборка User.
     * @return Выборка объектов класса UserResponse.
     */
    public PageDTO<UserResponse> toPageUser(Page<User> users) {
        return users == null ? PageDTO.empty() : new PageDTO<>(users.map(this::toResponse));
    }


}
