package com.example.bankcards.dto.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Фильтр для отбора пользователей.
 */
@Schema(description = "Фильтр для выборки пользователй из БД по заданным параметрам. Если поле равно null, то он не учитывается.")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserFilter {
    @Schema(description = "Имя пользователя", example = "Ivan")
    private String username;

    @Schema(description = "Искать по электронной почте", example = "@gmail.com")
    private String email;

    @Schema(description = "Искать активных или не активированных пользователей", example = "true")
    private Boolean enabled;

    @Schema(description = "Искать по заданной роли", example = "USER")
    private String role;
}
