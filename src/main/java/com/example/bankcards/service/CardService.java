package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.PageDTO;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.filters.CardFilter;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;


/**
 * Интерфейс сервисного слоя для работы с картами.
 */
public interface CardService {
    // функционал админа
    CardResponse createCard(CardCreateRequest request);
    CardResponse activateCard(UUID userId, Long id);
    CardResponse blockedCard(UUID userId, Long id);
    void deleteCard(UUID userId, Long id);
    PageDTO<CardResponse> getAllCards(CardFilter filter, Pageable pageable);

    // функционал юзера
    PageDTO<CardResponse> getUserCards(UUID userId, CardFilter filter, Pageable pageable);
    void requestToBlockingCard(UUID userId, Long cardId);
    void transferAmount(TransferRequest request);
    BigDecimal getBalance(UUID id, Long cardId);
}
