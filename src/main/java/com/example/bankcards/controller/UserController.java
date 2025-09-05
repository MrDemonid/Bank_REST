package com.example.bankcards.controller;

import com.example.bankcards.dto.PageDTO;
import com.example.bankcards.dto.SignUpRequest;
import com.example.bankcards.dto.UpdateUserRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.dto.filters.UserFilter;
import com.example.bankcards.exception.ErrorResponse;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@Tag(name = "Users controller", description = "REST API для управления пользователями. Требует роли 'ADMIN.")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Некорректные данные или ошибка сервиса", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован.", content = @Content(schema = @Schema())),
        @ApiResponse(responseCode = "403", description = "Нет прав доступа", content = @Content(schema = @Schema()))
})
@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;


    @Operation(summary = "Список пользователей", description = "Возвращает список пользователей с поддержкой фильтрации и пагинации.")
    @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/get-all")
    public ResponseEntity<PageDTO<UserResponse>> getAllUsers(@RequestBody UserFilter filter, Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(filter, pageable));
    }

    @Operation(summary = "Создание пользователя", description = "Добавляет нового пользователя в базу данных пользователей.")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно создан")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<UserResponse> createUser(@RequestBody SignUpRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @Operation(summary = "Обновление данных пользователя", description = "Обновляет данные существующего пользователя.")
    @ApiResponse(responseCode = "200", description = "Данные пользователя успешно обновлены")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<UserResponse> updateUser(@RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(request));
    }

    @Operation(summary = "Удаление пользователя", description = "Удаляет пользователя по его ID из базы данных.")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно удалён")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "ID пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

}
