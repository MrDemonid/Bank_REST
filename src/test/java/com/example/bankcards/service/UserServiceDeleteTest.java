package com.example.bankcards.service;

import com.example.bankcards.exception.UserOperationException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceDeleteTest {
    @Mock
    private UserRepository userRepository;

    // тестируем UserService
    @InjectMocks
    private UserServiceImpl userService;

    // данные для тестов
    UUID userId;

    /**
     * Подготавливаем данные для каждого тестового метода.
     */
    @BeforeEach
    public void setup() {
        userId = UUID.fromString("f1a09cf0-4a61-4f59-8e10-25f7ae8547d1");
    }


    /*
        Тест удаления существующего пользователя
     */
    @Test
    void testDeleteUser_success() {
        // моки
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        // выполняем
        assertDoesNotThrow(() -> userService.deleteUser(userId));

        // проверяем
        verify(userRepository).deleteById(userId);
    }

    /*
        Тест на не существующего юзера
     */
    @Test
    void testDeleteUser_failure_userNotFound() {
        // моки
        when(userRepository.existsById(userId)).thenReturn(false);

        // выполняем
        assertThrows(UserOperationException.class, () -> userService.deleteUser(userId));

        // проверяем
        verify(userRepository, never()).deleteById(userId);
    }

}
