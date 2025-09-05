package com.example.bankcards.service;

import com.example.bankcards.BaseCardServiceTest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.CardOperationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CardServiceBalanceTest extends BaseCardServiceTest {

    /*
        Тест с корректными параметрами.
     */
    @Test
    void balanceTest_success() {
        // моки
        when(userService.existsUser(userId)).thenReturn(true);
        when(cardRepository.findById(cardId)).thenReturn(Optional.ofNullable(card));

        // выполняем
        BigDecimal amount = cardService.getBalance(userId, cardId);

        // проверяем
        assertEquals(amount, balance);
    }

    /*
        Тест на неверного пользователя.
     */
    @Test
    void balanceTest_failure_userNotFound() {
        when(userService.existsUser(userId)).thenReturn(false);

        assertThrows(CardOperationException.class, () -> cardService.getBalance(userId, cardId));

        verify(cardRepository, never()).findById(cardId);
    }

    /*
        Тест на неверную карту.
     */
    @Test
    void balanceTest_failure_cardNotFound() {
        when(userService.existsUser(userId)).thenReturn(true);
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardOperationException.class, () -> cardService.getBalance(userId, cardId));
    }

    /*
        Тест на принадлежность карты другому пользователю.
     */
    @Test
    void balanceTest_failure_cardDeni() {
        when(userService.existsUser(userId)).thenReturn(true);
        when(cardRepository.findById(cardId)).thenReturn(Optional.ofNullable(card));
        card.setUserId(UUID.randomUUID());

        assertThrows(CardOperationException.class, () -> cardService.getBalance(userId, cardId));
    }

    /*
        Тест на не активную карту.
     */
    @Test
    void balanceTest_failure_cardIsNotActive() {
        when(userService.existsUser(userId)).thenReturn(true);
        when(cardRepository.findById(cardId)).thenReturn(Optional.ofNullable(card));
        card.setStatus(CardStatus.BLOCKED);

        assertThrows(CardOperationException.class, () -> cardService.getBalance(userId, cardId));
    }
}
