package com.example.bankcards.service;

import com.example.bankcards.BaseCardServiceTest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.PageDTO;
import com.example.bankcards.dto.filters.CardFilter;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.CardOperationException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;


public class CardServiceGetUserCardsTest extends BaseCardServiceTest {

    /*
        Тест с корректными данными.
     */
    @Test
    void testGetUserCard_success() {
        // моки
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> mockPage = new PageImpl<>(List.of(card));
        PageDTO<CardResponse> expected = new PageDTO<>(new PageImpl<>(List.of(cardResponse), pageable, 1));

        when(userService.existsUser(userId)).thenReturn(true);
        when(cardRepository.findAll(anySpec(), eq(pageable))).thenReturn(mockPage);
        when(cardMapper.toPageCard(mockPage)).thenReturn(expected);

        // выполняем
        PageDTO<CardResponse> result = cardService.getUserCards(userId, new CardFilter(null, null, CardStatus.ACTIVE, null), pageable);

        // проверка
        assertEquals(expected, result);
        verify(userService).existsUser(userId);
        verify(cardRepository).findAll(anySpec(), eq(pageable));
        verify(cardMapper).toPageCard(mockPage);
    }

    /*
        Тест на неверного пользователя.
     */
    @Test
    void testGetUserCard_failure_userNotFound() {
        when(userService.existsUser(userId)).thenReturn(false);

        assertThrows(CardOperationException.class, () -> cardService.getUserCards(userId, new CardFilter(), PageRequest.of(0, 10)));
    }

    /*
        Тест на ошибку выборки из БД.
     */
    @Test
    void testGetUserCards_failure_db() {
        when(userService.existsUser(userId)).thenReturn(true);
        when(cardRepository.findAll(anySpec(), any(Pageable.class))).thenThrow(new RuntimeException("DB error"));

        CardOperationException ex = assertThrows(CardOperationException.class, () ->
                cardService.getUserCards(userId, new CardFilter(), PageRequest.of(0, 10))
        );
    }

    // хэлпер, чтобы IDEA не ругалась на any(Specification.class)
    static <T> Specification<T> anySpec() {
        return ArgumentMatchers.any();
    }

}
