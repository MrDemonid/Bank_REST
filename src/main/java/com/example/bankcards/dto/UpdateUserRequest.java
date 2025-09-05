package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Запрос на Обновление данных пользователя.
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Запрос на создание нового пользователя.")
@Data
@NoArgsConstructor
public class UpdateUserRequest extends SignUpRequest {
    @Schema(description = "ID пользователя (для update)", example = "fb705a45-004d-4d60-989f-daa3e8572c49")
    private UUID id;

    @Schema(description = "Активен ли пользователь", example = "true")
    private boolean enabled;

    public UpdateUserRequest(String userName, String email, String password, Set<String> roles, UUID id, boolean enabled) {
        super(userName, email, password, roles);
        this.id = id;
        this.enabled = enabled;
    }
}
