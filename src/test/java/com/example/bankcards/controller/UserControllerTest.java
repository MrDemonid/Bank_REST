package com.example.bankcards.controller;

import com.example.bankcards.dto.PageDTO;
import com.example.bankcards.dto.SignUpRequest;
import com.example.bankcards.dto.UpdateUserRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.dto.filters.UserFilter;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.UserOperationException;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    // данные для тестов
    private String username;
    private String email;
    private UUID userId;
    private Set<String> allRoles;

    private UserResponse user;


    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        username = "test-user";
        email = "test-user@gmail.com";
        userId = UUID.randomUUID();
        allRoles = Set.of("ROLE_USER", "ROLE_ADMIN");
        user = new UserResponse(userId, username,  email, true, new HashSet<>(List.of("ROLE_USER")));
    }

/*
 * endpoint: /api/users/get-all
 */

    /*
        Позитивный сценарий.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_success() throws Exception {
        // моки
        PageDTO<UserResponse> page = new PageDTO<>(List.of(user), 1, 1, 10, 0);
        when(userService.getAllUsers(any(UserFilter.class), any(Pageable.class))).thenReturn(page);

        // выполняем и проверяем
        mockMvc.perform(post("/api/users/get-all")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserFilter())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$.content[0].email").value(email))
                .andExpect(jsonPath("$.content[0].enabled").value(user.isEnabled()))
                .andExpect(jsonPath("$.content[0].roles[*]", everyItem(in(allRoles.toArray()))))
                .andExpect(jsonPath("$.content[0].userName").value(username));
    }

    /*
        Проверяем отказ в доступе по недостатку прав.
     */
    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_fail_forbidden() throws Exception {
        // выполняем и проверяем
        mockMvc.perform(post("/api/users/get-all")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UserFilter())))
                .andExpect(status().isForbidden());
    }

    /*
        Проверка на возвращаемый результат при ошибке сервисного слоя.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldReturnErrorResponseOnException() throws Exception {
        when(userService.getAllUsers(any(UserFilter.class), any(Pageable.class)))
                .thenThrow(new CardOperationException(101, "Database error"));

        mockMvc.perform(post("/api/users/get-all")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserFilter())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(101))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message", Matchers.containsString("Database error")))
                .andExpect(jsonPath("$.path").value("/api/users/get-all"))
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()));
    }


    /*
     * endpoint: /api/users/create
     */

    /*
        Позитивный сценарий.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_success() throws Exception {
        // моки
        when(userService.createUser(any(SignUpRequest.class))).thenReturn(user);

        // выполняем и проверяем
        mockMvc.perform(post("/api/users/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignUpRequest())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(user.getUserId().toString()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.enabled").value(user.isEnabled()))
                .andExpect(jsonPath("$.roles[*]", everyItem(in(allRoles.toArray()))))
                .andExpect(jsonPath("$.userName").value(username));

        verify(userService).createUser(any(SignUpRequest.class));
    }

    /*
        Проверяем отказ в доступе по недостатку прав.
     */
    @Test
    @WithMockUser(roles = "USER")
    void createUser_fail_forbidden() throws Exception {
        // выполняем и проверяем
        mockMvc.perform(post("/api/users/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignUpRequest())))
                .andExpect(status().isForbidden());
    }

    /*
        Проверка на возвращаемый результат при ошибке сервисного слоя.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_shouldReturnErrorResponseOnException() throws Exception {
        when(userService.createUser(any(SignUpRequest.class))).thenThrow(new UserOperationException(101, "Database error"));

        mockMvc.perform(post("/api/users/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignUpRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(101))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message", Matchers.containsString("Database error")))
                .andExpect(jsonPath("$.path").value("/api/users/create"))
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()));
    }


    /*
     * endpoint: /api/users/update
     */

    /*
        Позитивный сценарий.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_success() throws Exception {
        // моки
        when(userService.updateUser(any(UpdateUserRequest.class))).thenReturn(user);

        // выполняем и проверяем
        mockMvc.perform(put("/api/users/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserRequest())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(user.getUserId().toString()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.enabled").value(user.isEnabled()))
                .andExpect(jsonPath("$.roles[*]", everyItem(in(allRoles.toArray()))))
                .andExpect(jsonPath("$.userName").value(username));
    }

    /*
        Проверяем отказ в доступе по недостатку прав.
     */
    @Test
    @WithMockUser(roles = "USER")
    void updateUser_fail_forbidden() throws Exception {
        // выполняем и проверяем
        mockMvc.perform(put("/api/users/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserRequest())))
                .andExpect(status().isForbidden());
    }

    /*
        Проверка на возвращаемый результат при ошибке сервисного слоя.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_shouldReturnErrorResponseOnException() throws Exception {
        when(userService.updateUser(any(UpdateUserRequest.class))).thenThrow(new UserOperationException(101, "Database error"));

        mockMvc.perform(put("/api/users/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(101))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message", Matchers.containsString("Database error")))
                .andExpect(jsonPath("$.path").value("/api/users/update"))
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()));
    }


/*
 * endpoint: /api/users/delete
 */
    /*
        Позитивный сценарий.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_success() throws Exception {
        // моки
        doNothing().when(userService).deleteUser(userId);

        // выполняем и проверяем
        mockMvc.perform(delete("/api/users/" + userId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    /*
        Проверяем отказ в доступе по недостатку прав.
     */
    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_fail_forbidden() throws Exception {
        // выполняем и проверяем
        mockMvc.perform(delete("/api/users/" + userId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    /*
        Проверка на возвращаемый результат при ошибке сервисного слоя.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_shouldReturnErrorResponseOnException() throws Exception {
        doThrow(new UserOperationException(101, "Database error")).when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/users/" + userId)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(101))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message", Matchers.containsString("Database error")))
                .andExpect(jsonPath("$.path").value("/api/users/" + userId))
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()));
    }


}
