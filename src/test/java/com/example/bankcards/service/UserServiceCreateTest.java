package com.example.bankcards.service;


import com.example.bankcards.dto.SignUpRequest;
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

@ExtendWith(MockitoExtension.class)
public class UserServiceCreateTest {

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
    SignUpRequest signUpRequest;
    User user;
    UserResponse userResponse;
    Set<Role> roles;
    Set<String> roleNames;

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

        signUpRequest = new SignUpRequest(username, email, password, roleNames);
        roles = new HashSet<>(List.of(new Role(1L, "ROLE_USER", "")));
        roleNames = new HashSet<>(List.of("ROLE_USER"));
        user = new User(userId, username, encodedPassword, email, enabled, roles);
        userResponse = new UserResponse(userId, username, email, enabled, roleNames);
    }

    /*
        Тест создания пользователя с корректно-заполненными полями
     */
    @Test
    public void createUserTest_success() {
        // моки
        when(userRepository.findByUsername(signUpRequest.getUserName())).thenReturn(Optional.empty());    // юзера нет в БД
        doNothing().when(userValidator).validateUserRequest(signUpRequest);
        when(roleMapper.toRoles(signUpRequest.getRoles())).thenReturn(roles);
        when(userMapper.toSignUpUser(signUpRequest, roles)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // вызов
        UserResponse response = userService.createUser(signUpRequest);

        // проверки
        assertNotNull(response);
        assertNotNull(response.getUserId());
        assertEquals(response.getUserName(), username);
        assertEquals(response.getEmail(), email);
        assertEquals(response.getRoles(), roleNames);
        assertEquals(response.isEnabled(), enabled);
        verify(userValidator).validateUserRequest(signUpRequest);     // проверяем, не забыли ли валидацию
        verify(userRepository).save(user);                          // и что сохраняли в БД
    }

    /*
        Проверка поведения при попытке создать дубликат пользователя.
     */
    @Test
    public void createUserTest_failure_user_already_exists() {
        // моки
        when(userRepository.findByUsername(signUpRequest.getUserName())).thenReturn(Optional.of(user));

        // вызов, с ожиданием исключения
        assertThrows(UserOperationException.class, () -> userService.createUser(signUpRequest));

        // проверки
        // убеждаемся, что остальные методы не вызывались
        verifyNoMoreInteractions(userValidator, userRepository, userMapper, roleMapper);
    }

    /*
        Проверяем исключение при валидации входных данных.
     */
    @Test
    public void createUserTest_failure_validation() {
        // моки
        when(userRepository.findByUsername(signUpRequest.getUserName())).thenReturn(Optional.empty());
        doThrow(new UserOperationException(101, "test exception")).when(userValidator).validateUserRequest(signUpRequest);

        // вызов, с ожиданием исключения
        assertThrows(UserOperationException.class, () -> userService.createUser(signUpRequest));

        // проверки
        verify(userValidator).validateUserRequest(signUpRequest);     // проверяем, не забыли ли валидацию

        // убеждаемся, что остальные методы не вызывались
        verifyNoMoreInteractions(userRepository, userMapper, roleMapper);
    }



}
