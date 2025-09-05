package com.example.bankcards.service;

import com.example.bankcards.dto.UpdateUserRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.auth.Role;
import com.example.bankcards.entity.auth.User;
import com.example.bankcards.exception.UserOperationException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CustomPasswordEncoder;
import com.example.bankcards.service.mappers.RoleMapper;
import com.example.bankcards.service.mappers.UserMapper;
import com.example.bankcards.service.validators.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceUpdateTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomPasswordEncoder customPasswordEncoder;

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserValidator userValidator;
    @Mock
    private RoleMapper roleMapper;


    // тестируем UserService
    @InjectMocks
    private UserServiceImpl userService;

    // данные для тестов
    UUID userId;
    String username;
    String email;
    String password;
    String encodedPassword;
    boolean enabled;
    UpdateUserRequest updateUserRequest;
    User user;
    UserResponse userResponse;
    Set<String> roleNames;
    Set<Role> roles;

    /**
     * Подготавливаем данные для каждого тестового метода.
     */
    @BeforeEach
    public void setup() {
        userId = UUID.fromString("f1a09cf0-4a61-4f59-8e10-25f7ae8547d1");
        username = "test-user";
        email = "test-user@gmail.com";
        password = "test-password";
        encodedPassword = "$2a$12$PHyg.tvGjlPEFUWO/aY2SeI6ZD/usd8oeQE/8sb/DlRkzwYTGg9zm";
        enabled = true;

        updateUserRequest = new UpdateUserRequest(username, email, password, roleNames, userId, enabled);
        roleNames = new HashSet<>(List.of("ROLE_USER"));
        roles = new HashSet<>(List.of(new Role(1L, "ROLE_USER", "")));
        user = new User(userId, "test", encodedPassword, "test@mail.ru", false, new HashSet<>(List.of(new Role(1L, "ROLE_ADMIN", ""))));
        userResponse = new UserResponse(userId, username, email, enabled, roleNames);
    }


    /*
        Тест обновления данных с корректно-заполненными полями
     */
    @Test
    void updateUserTest_success() {
        // моки
        when(userRepository.findById(updateUserRequest.getId())).thenReturn(Optional.ofNullable(user));
        doNothing().when(userValidator).validateUserRequest(updateUserRequest);
        when(customPasswordEncoder.encode(updateUserRequest.getPassword())).thenReturn(encodedPassword);
        when(roleMapper.toRoles(updateUserRequest.getRoles())).thenReturn(roles);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // вызов
        UserResponse response = userService.updateUser(updateUserRequest);

        // проверки
        assertNotNull(response);
        assertEquals(user.getPassword(), encodedPassword);
        assertEquals(user.getRoles(), roles);
        assertEquals(response.getUserId(), userId);
        assertEquals(response.getUserName(), username);
        assertEquals(response.getEmail(), email);
        assertEquals(response.getRoles(), roleNames);
        assertEquals(response.isEnabled(), enabled);
        verify(userValidator).validateUserRequest(updateUserRequest);     // проверяем, не забыли ли валидацию
        verify(userRepository).save(user);                          // и что сохраняли в БД
    }

    /*
        Тест на не существующего пользователя.
     */
    @Test
    void updateUserTest_failure_userNotFound() {
        // моки
        when(userRepository.findById(updateUserRequest.getId())).thenReturn(Optional.empty());

        // вызов
        assertThrows(UserOperationException.class, () -> userService.updateUser(updateUserRequest));

        // проверки, убеждаемся что остальные методы не вызывались
        verifyNoMoreInteractions(userValidator, userRepository, userMapper);
    }

    /*
        Тест на исключение при валидации входных данных
     */
    @Test
    void updateUserTest_failure_validation() {
        // моки
        when(userRepository.findById(updateUserRequest.getId())).thenReturn(Optional.ofNullable(user));
        doThrow(new UserOperationException(101, "test exception")).when(userValidator).validateUserRequest(updateUserRequest);

        // вызов, ожидаем исключения
        assertThrows(UserOperationException.class, () -> userService.updateUser(updateUserRequest));

        // проверки
        verify(userValidator).validateUserRequest(updateUserRequest);     // не забыли валидацию данных?
        verifyNoMoreInteractions(userRepository, userMapper);
    }


}
