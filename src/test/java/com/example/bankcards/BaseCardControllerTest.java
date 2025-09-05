package com.example.bankcards;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;


@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseCardControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected CardService cardService;

    // данные для тестов
    protected UUID userId;
    protected Long cardId;
    protected String cardNumber;
    protected String cardHideNumber;
    protected YearMonth expiryDate;
    protected CardResponse cardResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cardId = 1L;
        cardNumber = "1111222233334444";
        cardHideNumber = "**** **** **** 4444";
        expiryDate = YearMonth.now().plusYears(3);
        cardResponse = new CardResponse(cardId, cardHideNumber, expiryDate, CardStatus.ACTIVE, BigDecimal.ZERO, userId);
    }

}
