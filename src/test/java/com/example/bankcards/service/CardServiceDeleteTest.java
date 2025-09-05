package com.example.bankcards.service;

import com.example.bankcards.BaseCardServiceTest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.CardOperationException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


public class CardServiceDeleteTest extends BaseCardServiceTest {

    /*
        Тест с корректными параметрами.
     */
    @Test
    void testDeleteCard_success() {
        // моки
        when(cardRepository.findById(cardId)).thenReturn(Optional.ofNullable(card));
        doNothing().when(cardRepository).delete(card);

        // выполняем
        assertDoesNotThrow(() -> cardService.deleteCard(userId, cardId));

        verify(cardRepository).delete(card);
    }

    /*
        Тест с некорректным ID карты.
     */
    @Test
    void testDeleteCard_failure_WrongCardId() {
        // моки
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // выполняем
        assertThrows(CardOperationException.class, () -> cardService.deleteCard(userId, cardId));

        // проверяем
        verify(cardRepository, never()).delete(any(Card.class));
    }

    /*
        Тест на ошибку в БД при удалении.
     */
    @Test
    void testDeleteCard_failure_WrongDelete() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.ofNullable(card));
        doThrow(CardOperationException.class).when(cardRepository).delete(card);

        assertThrows(CardOperationException.class, () -> cardService.deleteCard(userId, cardId));
    }
}
