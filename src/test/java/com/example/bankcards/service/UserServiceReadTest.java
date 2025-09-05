package com.example.bankcards.service;

import com.example.bankcards.dto.PageDTO;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.dto.filters.UserFilter;
import com.example.bankcards.entity.auth.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.mappers.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;

import java.util.List;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UserServiceReadTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    // тестируем UserService
    @InjectMocks
    private UserServiceImpl userService;

    /**
     * Подготавливаем данные для каждого тестового метода.
     */
    @BeforeEach
    public void setup() {
    }

    /*
        Проверка поведения при нормальной работе БД.
     */
    @Test
    void getAllUsers_success() {
        UserFilter filter = new UserFilter();
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> usersPage = new PageImpl<>(List.of(new User()));

        when(userRepository.findAll(anySpec(), eq(pageable))).thenReturn(usersPage);

        PageDTO<UserResponse> expected = new PageDTO<>();
        when(userMapper.toPageUser(usersPage)).thenReturn(expected);

        PageDTO<UserResponse> result = userService.getAllUsers(filter, pageable);

        assertSame(expected, result);
    }

    /*
        Проверка реакции на ошибку БД. Должна вернуть пустой список.
     */
    @Test
    void getAllUsers_exception_returnsEmpty() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(anySpec(), eq(pageable))).thenThrow(new RuntimeException());

        PageDTO<UserResponse> result = userService.getAllUsers(new UserFilter(), pageable);

        assertTrue(PageDTO.isEmpty(result));
    }


    // хэлпер, чтобы IDEA не ругалась на any(Specification.class)
    static <T> Specification<T> anySpec() {
        return ArgumentMatchers.any();
    }
}
