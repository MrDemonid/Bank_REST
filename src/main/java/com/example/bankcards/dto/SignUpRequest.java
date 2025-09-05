package com.example.bankcards.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Запрос на создание нового пользователя.
 */
@Schema(description = "Запрос на создание нового пользователя.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    @Schema(description = "Имя пользователя", example = "Andrey")
    private String userName;

    @Schema(description = "Электронная почта", example = "andrey@gmail.com")
    private String email;

    @Schema(description = "Пароль", example = "12345678")
    private String password;

    @Schema(description = "Роли пользователя", example = "[\"USER\", \"ADMIN\"]")
    private Set<String> roles;
}
