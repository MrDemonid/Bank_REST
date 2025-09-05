package com.example.bankcards;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardServiceImpl;
import com.example.bankcards.service.UserService;
import com.example.bankcards.service.mappers.CardMapper;
import com.example.bankcards.util.CardNumberHasher;
import com.example.bankcards.util.CardUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public abstract class BaseCardServiceTest {
    @Mock
    protected CardRepository cardRepository;
    @Mock
    protected UserService userService;
    @Mock
    protected CardMapper cardMapper;

    @InjectMocks
    protected CardServiceImpl cardService;

    // данные для тестов
    protected UUID userId;
    protected Long cardId;
    protected String cardNumber;
    protected String cardNumberHmac;
    protected String cardEncodeNumber;
    protected YearMonth expiryDate;
    protected BigDecimal balance;
    protected CardStatus cardStatus;

    protected CardCreateRequest cardCreateRequest;
    protected CardResponse cardResponse;

    protected Card card;

    /**
     * Подготавливаем данные для каждого тестового метода.
     */
    @BeforeEach
    public void setup() {
        userId = UUID.randomUUID();
        cardId = 1L;
        cardNumber = "1111222233334444";
        cardNumberHmac = CardNumberHasher.hmacSha256(CardUtil.normalizeCardNumber(cardNumber));
        cardEncodeNumber = "**** **** **** 4444";
        expiryDate = YearMonth.now().plusYears(3);
        balance = BigDecimal.valueOf(100);
        cardStatus = CardStatus.ACTIVE;

        cardCreateRequest = new CardCreateRequest(userId, cardNumber, expiryDate, new BigDecimal("100.50"));
        cardResponse = new CardResponse(cardId, cardEncodeNumber, expiryDate, CardStatus.ACTIVE, balance, userId);

        card = new Card(cardId, cardNumber, cardNumberHmac, expiryDate, CardStatus.ACTIVE, balance, userId);
    }

}
