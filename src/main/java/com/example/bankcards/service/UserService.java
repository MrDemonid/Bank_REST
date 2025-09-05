package com.example.bankcards.service;

import com.example.bankcards.dto.PageDTO;
import com.example.bankcards.dto.SignUpRequest;
import com.example.bankcards.dto.UpdateUserRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.dto.filters.UserFilter;
import org.springframework.data.domain.Pageable;

import java.util.UUID;


/**
 * Интерфейс сервисного слоя для работы с пользователями.
 */
public interface UserService {
    Boolean existsUser(UUID userId);
    UserResponse createUser(SignUpRequest userRequest);                         // C
    PageDTO<UserResponse> getAllUsers(UserFilter filter, Pageable pageable);    // R
    UserResponse updateUser(UpdateUserRequest updateUserRequest);               // U
    void deleteUser(UUID userId);                                               // D
}
