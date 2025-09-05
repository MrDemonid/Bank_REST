package com.example.bankcards.service;


import com.example.bankcards.BaseCardServiceTest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.CardOperationException;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;


public class CardServiceActivateBlockingTest extends BaseCardServiceTest {

    /*
        Тест активации с корректными параметрами.
     */
    @Test
    void testActivateCard_success() {
        // моки
        when(cardRepository.findById(1L)).thenReturn(Optional.ofNullable(card));
        when(cardRepository.save(card)).thenAnswer(e -> {
            Card res = e.getArgument(0);
            card.setId(res.getId());
            card.setCardNumber(res.getCardNumber());
            card.setStatus(res.getStatus());
            card.setExpiryDate(res.getExpiryDate());
            card.setBalance(res.getBalance());
            card.setUserId(res.getUserId());
            return card;
        });
        when(cardMapper.toCardResponse(card)).thenReturn(cardResponse);
        card.setStatus(CardStatus.BLOCKED);

        // выполняем
        CardResponse response = cardService.activateCard(userId, cardId);

        // проверяем
        assertNotNull(response);
        assertEquals(CardStatus.ACTIVE, card.getStatus());
        verify( cardRepository).save(card);
        verify(cardMapper).toCardResponse(card);
    }

    /*
        Тест на неверный ID карты.
     */
    @Test
    void testActivateCard_failure_WrongCardId() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // выполняем
        assertThrows(CardOperationException.class, () -> cardService.activateCard(userId, cardId));

        // проверяем
        verifyNoMoreInteractions(cardRepository, cardMapper);
    }

    /*
        Тест на неверный ID пользователя.
     */
    @Test
    void testActivateCard_failure_WrongUserId() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // выполняем
        assertThrows(CardOperationException.class, () -> cardService.activateCard(UUID.randomUUID(), cardId));

        // проверяем
        verifyNoMoreInteractions(cardRepository, cardMapper);
    }

    /*
        Непредвиденная ошибка записи в БД.
     */
    @Test
    void testActivateCard_failure_UnexpectedError() {
        // моки
        when(cardRepository.findById(cardId)).thenReturn(Optional.ofNullable(card));
        doThrow(CardOperationException.class).when(cardRepository).save(card);

        // выполняем
        assertThrows(CardOperationException.class, () -> cardService.activateCard(userId, cardId));

        // проверяем
        verifyNoMoreInteractions(cardMapper);
    }

    /*
        Тест блокирования с корректными параметрами.
     */
    @Test
    void testBlockingCard_success() {
        // моки
        when(cardRepository.findById(cardId)).thenReturn(Optional.ofNullable(card));
        when(cardRepository.save(card)).thenAnswer(e -> {
            Card res = e.getArgument(0);
            card.setId(res.getId());
            card.setCardNumber(res.getCardNumber());
            card.setStatus(res.getStatus());
            card.setExpiryDate(res.getExpiryDate());
            card.setBalance(res.getBalance());
            card.setUserId(res.getUserId());
            return card;
        });
        when(cardMapper.toCardResponse(card)).thenReturn(cardResponse);
        card.setStatus(CardStatus.ACTIVE);

        // выполняем
        CardResponse response = cardService.blockedCard(userId, cardId);

        // проверяем
        assertNotNull(response);
        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
        verify(cardMapper).toCardResponse(card);
    }

    /*
        Тест на неверный ID карты.
     */
    @Test
    void testBlockingCard_failure_WrongCardId() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // выполняем
        assertThrows(CardOperationException.class, () -> cardService.blockedCard(userId, cardId));

        // проверяем
        verifyNoMoreInteractions(cardRepository, cardMapper);
    }

    /*
        Тест на неверный ID пользователя.
     */
    @Test
    void testBlockingCard_failure_WrongUserId() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // выполняем
        assertThrows(CardOperationException.class, () -> cardService.blockedCard(UUID.randomUUID(), cardId));

        // проверяем
        verifyNoMoreInteractions(cardRepository, cardMapper);
    }

    /*
        Непредвиденная ошибка записи в БД.
     */
    @Test
    void testBlockingCard_failure_UnexpectedError() {
        // моки
        when(cardRepository.findById(1L)).thenReturn(Optional.ofNullable(card));
        doThrow(CardOperationException.class).when(cardRepository).save(card);

        // выполняем
        assertThrows(CardOperationException.class, () -> cardService.blockedCard(userId, cardId));

        // проверяем
        verifyNoMoreInteractions(cardMapper);
    }

}
