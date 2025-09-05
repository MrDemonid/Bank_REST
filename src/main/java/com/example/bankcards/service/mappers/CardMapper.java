package com.example.bankcards.service.mappers;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.PageDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.util.CardUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * Конвертер для сущности Card.
 */
@Component
public class CardMapper {

    public CardResponse toCardResponse(Card card) {
        return new CardResponse(
                card.getId(),
                CardUtil.getMaskedNumber(card.getCardNumber()),
                card.getExpiryDate(),
                card.getStatus(),
                card.getBalance(),
                card.getUserId()
        );
    }

    public PageDTO<CardResponse> toPageCard(Page<Card> cards) {
        return cards == null ? PageDTO.empty() : new PageDTO<>(cards.map(this::toCardResponse));
    }
}
