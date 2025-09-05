package com.example.bankcards.service;

import com.example.bankcards.BaseCardServiceTest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.util.CardNumberHasher;
import com.example.bankcards.util.CardUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CardServiceTransferTest extends BaseCardServiceTest {

    TransferRequest transferRequest;
    Card cardTo;

    /**
     * Подготавливаем данные для каждого тестового метода.
     */
    @BeforeEach
    public void setup() {
        super.setup();
        transferRequest = new TransferRequest(userId, 1L, 2L, new BigDecimal("30.0"));
        cardTo = new Card(2L, "5555666677778888", CardNumberHasher.hmacSha256(CardUtil.normalizeCardNumber("5555666677778888")), expiryDate, CardStatus.ACTIVE, balance, userId);
    }

    /*
        Тест с корректными данными.
     */
    @Test
    void transferCard_success() {
        // моки
        when(userService.existsUser(transferRequest.getUserId())).thenReturn(true);
        when(cardRepository.findById(transferRequest.getFromCardId())).thenReturn(Optional.ofNullable(card));
        when(cardRepository.findById(transferRequest.getToCardId())).thenReturn(Optional.ofNullable(cardTo));
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // выполняем
        assertDoesNotThrow(() -> cardService.transferAmount(transferRequest));

        // проверяем
        assertEquals(new BigDecimal("70.0"), card.getBalance());
        assertEquals(new BigDecimal("130.0"), cardTo.getBalance());

        verify(cardRepository).save(card);
        verify(cardRepository).save(cardTo);
    }

    /*
        Тест на отсутствие пользователя.
     */
    @Test
    void transferCard_failure_userNotFound() {
        when(userService.existsUser(transferRequest.getUserId())).thenReturn(false);

        assertThrows(CardOperationException.class, () -> cardService.transferAmount(transferRequest));

        verifyNoMoreInteractions(cardRepository);
    }

    /*
        Тест на отсутствие одной из карт.
     */
    @Test
    void transferCard_failure_cardNotFoundFrom() {
        when(userService.existsUser(transferRequest.getUserId())).thenReturn(true);
        when(cardRepository.findById(transferRequest.getFromCardId())).thenReturn(Optional.empty());
        when(cardRepository.findById(transferRequest.getToCardId())).thenReturn(Optional.ofNullable(cardTo));

        assertThrows(CardOperationException.class, () -> cardService.transferAmount(transferRequest));

        verify(cardRepository, never()).save(any());
    }

    /*
        Тест на отсутствие одной из карт.
     */
    @Test
    void transferCard_failure_cardNotFoundTo() {
        when(userService.existsUser(transferRequest.getUserId())).thenReturn(true);
        when(cardRepository.findById(transferRequest.getFromCardId())).thenReturn(Optional.ofNullable(card));
        when(cardRepository.findById(transferRequest.getToCardId())).thenReturn(Optional.empty());

        assertThrows(CardOperationException.class, () -> cardService.transferAmount(transferRequest));

        verify(cardRepository, never()).save(any());
    }

    /*
        Тест на попытку использования для перевода одной и той же карты.
     */
    @Test
    void transferCard_failure_cardAlreadyUsed() {
        when(userService.existsUser(transferRequest.getUserId())).thenReturn(true);
        when(cardRepository.findById(transferRequest.getFromCardId())).thenReturn(Optional.ofNullable(card));
        when(cardRepository.findById(transferRequest.getToCardId())).thenReturn(Optional.ofNullable(cardTo));
        card.setId(cardTo.getId());

        assertThrows(CardOperationException.class, () -> cardService.transferAmount(transferRequest));

        verify(cardRepository, never()).save(any());
    }

    /*
        Тест на попытку перевести другому пользователю.
     */
    @Test
    void transferCard_failure_WrongUserId() {
        when(userService.existsUser(transferRequest.getUserId())).thenReturn(true);
        when(cardRepository.findById(transferRequest.getFromCardId())).thenReturn(Optional.ofNullable(card));
        when(cardRepository.findById(transferRequest.getToCardId())).thenReturn(Optional.ofNullable(cardTo));
        cardTo.setUserId(UUID.randomUUID());

        assertThrows(CardOperationException.class, () -> cardService.transferAmount(transferRequest));

        verify(cardRepository, never()).save(any());
    }

    /*
        Тест на попытку операции с не активной картой.
     */
    @Test
    void transferCard_failure_cardNotActiveFrom() {
        when(userService.existsUser(transferRequest.getUserId())).thenReturn(true);
        when(cardRepository.findById(transferRequest.getFromCardId())).thenReturn(Optional.ofNullable(card));
        when(cardRepository.findById(transferRequest.getToCardId())).thenReturn(Optional.ofNullable(cardTo));
        card.setStatus(CardStatus.BLOCKED);

        assertThrows(CardOperationException.class, () -> cardService.transferAmount(transferRequest));

        verify(cardRepository, never()).save(any());
    }

    /*
        Тест на попытку операции с не активной картой.
     */
    @Test
    void transferCard_failure_cardNotActiveTo() {
        when(userService.existsUser(transferRequest.getUserId())).thenReturn(true);
        when(cardRepository.findById(transferRequest.getFromCardId())).thenReturn(Optional.ofNullable(card));
        when(cardRepository.findById(transferRequest.getToCardId())).thenReturn(Optional.ofNullable(cardTo));
        cardTo.setStatus(CardStatus.BLOCKED);

        assertThrows(CardOperationException.class, () -> cardService.transferAmount(transferRequest));

        verify(cardRepository, never()).save(any());
    }

    /*
        Тест на недостаток средств.
     */
    @Test
    void transferCard_failure_WrongBalance() {
        // моки
        when(userService.existsUser(transferRequest.getUserId())).thenReturn(true);
        when(cardRepository.findById(transferRequest.getFromCardId())).thenReturn(Optional.ofNullable(card));
        when(cardRepository.findById(transferRequest.getToCardId())).thenReturn(Optional.ofNullable(cardTo));
        transferRequest.setAmount(card.getBalance().add(BigDecimal.ONE));

        // выполняем
        assertThrows(CardOperationException.class, () -> cardService.transferAmount(transferRequest));

        // проверяем
        assertEquals(card.getBalance(), balance);
        assertEquals(card.getBalance(), balance);

        verify(cardRepository, never()).save(any());
    }


}
