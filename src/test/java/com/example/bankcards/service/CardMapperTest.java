package com.example.bankcards.service;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.mappers.CardMapper;
import com.example.bankcards.util.CardNumberHasher;
import com.example.bankcards.util.CardUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.example.bankcards.dto.PageDTO;


@ExtendWith(MockitoExtension.class)
public class CardMapperTest {


    private final CardMapper cardMapper = new CardMapper();

    /*
        Проверка на корректность конвертации, убеждаемся, что поле номера карты будет скрыто.
     */
    @Test
    void toCardResponse_correctCard() {
        // данные
        Long cardId = 1L;
        UUID userId = UUID.randomUUID();
        String cardNumber = "1111222233334444";
        String cardNumberHmac = CardNumberHasher.hmacSha256(cardNumber);
        YearMonth expiry = YearMonth.now().plusYears(3);
        Card card = new Card(cardId, cardNumber, cardNumberHmac, expiry, CardStatus.ACTIVE, new BigDecimal("100.50"), userId);

        // выполняем
        CardResponse response = cardMapper.toCardResponse(card);

        // проверка
        assertThat(response.getId()).isEqualTo(cardId);
        assertThat(response.getExpiryDate()).isEqualTo(expiry);
        assertThat(response.getStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(response.getBalance()).isEqualByComparingTo("100.50");
        assertThat(response.getUserId()).isEqualTo(userId);
        // проверяем, что номер карты не утекает
        assertThat(response.getCardNumber()).isEqualTo("**** **** **** 4444");
    }

    /*
        Проверяем конвертацию с пагинацией.
     */
    @Test
    void toPageCard_success() {
        // данные
        Card card1 = new Card(
                1L,
                "1111222233334444",
                CardNumberHasher.hmacSha256(CardUtil.normalizeCardNumber("1111222233334444")),
                YearMonth.now().plusYears(3),
                CardStatus.ACTIVE,
                BigDecimal.ZERO,
                UUID.randomUUID()
        );
        Card card2 = new Card(
                2L,
                "5555 6666 7777 8888",
                CardNumberHasher.hmacSha256(CardUtil.normalizeCardNumber("5555 6666 7777 8888")),
                YearMonth.now().plusYears(3),
                CardStatus.BLOCKED,
                new BigDecimal("42.00"),
                UUID.randomUUID()
        );

        Page<Card> page = new PageImpl<>(List.of(card1, card2), PageRequest.of(0, 2), 2);

        // выполняем
        PageDTO<CardResponse> dto = cardMapper.toPageCard(page);

        // проверяем
        assertThat(dto.getContent().size()).isEqualTo(2);

        CardResponse r1 = dto.getContent().get(0);
        assertThat(r1.getCardNumber()).isEqualTo("**** **** **** 4444");
        assertThat(r1.getStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(r1.getBalance()).isEqualByComparingTo("0");

        CardResponse r2 = dto.getContent().get(1);
        assertThat(r2.getCardNumber()).isEqualTo("**** **** **** 8888");
        assertThat(r2.getStatus()).isEqualTo(CardStatus.BLOCKED);
        assertThat(r2.getBalance()).isEqualByComparingTo("42.00");
    }


    @Test
    void toPageCard_whenNullInput() {
        // выполняем
        PageDTO<CardResponse> dto = cardMapper.toPageCard(null);

        // проверяем
        assertThat(dto).isNotNull();
        assertThat(dto.getContent().isEmpty()).isEqualTo(Boolean.TRUE);
    }

    @Test
    void toPageCard_whenEmptyPage() {
        // данные
        Page<Card> emptyPage = Page.empty();

        // выполняем
        PageDTO<CardResponse> dto = cardMapper.toPageCard(emptyPage);

        // проверяем
        assertThat(dto.getContent().isEmpty()).isEqualTo(Boolean.TRUE);
    }
}

