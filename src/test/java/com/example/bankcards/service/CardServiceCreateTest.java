package com.example.bankcards.service;

import com.example.bankcards.BaseCardServiceTest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.CardOperationException;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class CardServiceCreateTest extends BaseCardServiceTest {

    /*
        Тест на корректные данные.
        Маппер проверен отдельным тестом и точно возвращает скрытый номер карты. Проверку же на
        отсутствие утечки номера карты окончательно поставит тест на контроллер.
    */
    @Test
    void cardCreateTest_success() {
        // моки
        when(userService.existsUser(cardCreateRequest.getUserId())).thenReturn(Boolean.TRUE);
        when(cardRepository.existsByCardNumberHmac(cardNumberHmac)).thenReturn(Boolean.FALSE);
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.toCardResponse(card)).thenReturn(cardResponse);

        // выполняем
        CardResponse response = cardService.createCard(cardCreateRequest);

        // проверяем
        assertThat(response).isEqualTo(cardResponse);

        // перехват аргумента
        verify(cardRepository).save(any(Card.class));
        verify(cardMapper).toCardResponse(card);
    }

    /*
        Тест на некорректный номер карты.
     */
    @Test
    void cardCreateTest_failure_CardNumberWrong() {
        // моки
        cardCreateRequest.setCardNumber("1234567890");

        // выполняем
        assertThrows(CardOperationException.class, () -> cardService.createCard(cardCreateRequest));

        // проверка
        verifyNoMoreInteractions(userService, cardRepository, cardMapper);
    }

    /*
        Тест на неверную дату.
     */
    @Test
    void cardCreateTest_failure_ExpiryDateWrong() {
        // моки
        cardCreateRequest.setExpiryDate(YearMonth.now());

        // выполняем
        assertThrows(CardOperationException.class, () -> cardService.createCard(cardCreateRequest));

        // проверка
        verifyNoMoreInteractions(userService, cardRepository, cardMapper);
    }

    /*
        Тест на неправильный ID пользователя.
     */
    @Test
    void cardCreateTest_failure_UserNotFound() {
        // моки
        when(userService.existsUser(cardCreateRequest.getUserId())).thenReturn(Boolean.FALSE);

        // выполняем
        assertThrows(CardOperationException.class, () -> cardService.createCard(cardCreateRequest));

        verifyNoMoreInteractions(cardRepository, cardMapper);
    }

    /*
        Тест на уже существующий номер карты.
     */
    @Test
    void cardCreateTest_failure_CardAlreadyExists() {
        // моки
        when(userService.existsUser(cardCreateRequest.getUserId())).thenReturn(Boolean.TRUE);
        when(cardRepository.existsByCardNumberHmac(cardNumberHmac)).thenReturn(Boolean.TRUE);

        // выполняем
        assertThrows(CardOperationException.class, () -> cardService.createCard(cardCreateRequest));

        verifyNoMoreInteractions(cardRepository, cardMapper);
    }
}
