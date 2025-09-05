package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Информация о пользователе. Возвращается из REST API.
 */
@Schema(description = "Информация о пользователе.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    @Schema(description = "ID пользователя", example = "fb705a45-004d-4d60-989f-daa3e8572c49")
    private UUID userId;

    @Schema(description = "Имя пользователя", example = "Ivan")
    private String userName;

    @Schema(description = "Электронная почта", example = "ivan@gmail.com")
    private String email;

    @Schema(description = "Активен ли пользователь", example = "true")
    private boolean enabled;

    @Schema(description = "Роли пользователя", example = "[\"USER\", \"ADMIN\"]")
    private Set<String> roles;
}

