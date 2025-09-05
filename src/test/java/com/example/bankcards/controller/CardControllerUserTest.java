package com.example.bankcards.controller;

import com.example.bankcards.BaseCardControllerTest;
import com.example.bankcards.dto.*;
import com.example.bankcards.dto.filters.CardFilter;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class CardControllerUserTest extends BaseCardControllerTest {

    @Nested
    @DisplayName("POST /api/cards/get-cards/{userId}")
    class GetCards {

        @Test
        @DisplayName("Позитивный сценарий")
        @WithMockUser("USER")
        void success() throws Exception {
            PageDTO<CardResponse> response = new PageDTO<>(List.of(cardResponse), 1, 1, 10, 0);
            CardFilter filter = new CardFilter(null, null, CardStatus.ACTIVE, null);

            when(cardService.getUserCards(eq(userId), any(CardFilter.class), any(Pageable.class))).thenReturn(response);

            mockMvc.perform(post("/api/cards/get-cards/" + userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(filter)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content[0].cardNumber", is(cardHideNumber)))
                    .andExpect(jsonPath("$.content[0].expiryDate", is(expiryDate.toString())))
                    .andExpect(jsonPath("$.content[0].status", is(CardStatus.ACTIVE.toString())))
                    .andExpect(jsonPath("$.content[0].userId", is(userId.toString())))
                    .andExpect(jsonPath("$.content[0].balance").value(BigDecimal.ZERO))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(cardService, times(1)).getUserCards(eq(userId), any(CardFilter.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Не авторизированный пользователь")
        void unauthorized() throws Exception {
            mockMvc.perform(post("/api/cards/get-cards/" + userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CardFilter())))
                    .andExpect(status().isUnauthorized());
            verify(cardService, never()).getUserCards(eq(userId), any(CardFilter.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Пустой результат")
        @WithMockUser(roles = "USER")
        void emptyResult() throws Exception {
            when(cardService.getUserCards(eq(userId), any(CardFilter.class), any(Pageable.class))).thenReturn(PageDTO.empty());

            mockMvc.perform(post("/api/cards/get-cards/" + userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CardFilter())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalPages").value(0))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Ошибка в сервисном слое")
        @WithMockUser(roles = "USER")
        void exception() throws Exception {
            doThrow(new CardOperationException(101, "Database error")).when(cardService).getUserCards(eq(userId), any(CardFilter.class), any(Pageable.class));

            // выполняем и проверяем
            mockMvc.perform(post("/api/cards/get-cards/" + userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CardFilter())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(101))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.message", Matchers.containsString("Database error")))
                    .andExpect(jsonPath("$.path").value("/api/cards/get-cards/" + userId))
                    .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()));
        }
    }

    @Nested
    @DisplayName("GET /api/cards/request-block/{userId}/{cardId}")
    class RequestToBlockCard {

        @Test
        @DisplayName("Позитивный сценарий")
        @WithMockUser(roles = "USER")
        void success() throws Exception {
            doNothing().when(cardService).requestToBlockingCard(userId, cardId);

            mockMvc.perform(get("/api/cards/request-block/" + userId + "/" + cardId)
                            .with(csrf()))
                    .andExpect(status().isOk());

            verify(cardService, times(1)).requestToBlockingCard(userId, cardId);
        }

        @Test
        @DisplayName("Не авторизированный пользователь")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/cards/request-block/" + userId + "/" + cardId)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
            verify(cardService, never()).requestToBlockingCard(userId, cardId);
        }

        @Test
        @DisplayName("Ошибка в сервисном слое")
        @WithMockUser(roles = "USER")
        void exception() throws Exception {
            doThrow(new CardOperationException(101, "Database error")).when(cardService).requestToBlockingCard(userId, cardId);
            mockMvc.perform(get("/api/cards/request-block/" + userId + "/" + cardId)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(101))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.message", Matchers.containsString("Database error")))
                    .andExpect(jsonPath("$.path").value("/api/cards/request-block/" + userId + "/" + cardId))
                    .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()));
        }
    }

    @Nested
    @DisplayName("POST /api/cards/transfer")
    class TransferAmount {

        @Test
        @DisplayName("Позитивный сценарий")
        @WithMockUser(roles = "USER")
        void success() throws Exception {
            doNothing().when(cardService).transferAmount(any(TransferRequest.class));

            mockMvc.perform(post("/api/cards/transfer")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new TransferRequest())))
                    .andExpect(status().isOk());

            verify(cardService, times(1)).transferAmount(any(TransferRequest.class));
        }

        @Test
        @DisplayName("Не авторизированный пользователь")
        void unauthorized() throws Exception {
            mockMvc.perform(post("/api/cards/transfer")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new TransferRequest())))
                    .andExpect(status().isUnauthorized());
            verify(cardService, never()).transferAmount(any(TransferRequest.class));
        }

        @Test
        @DisplayName("Ошибка в сервисном слое")
        @WithMockUser(roles = "USER")
        void exception() throws Exception {
            doThrow(new CardOperationException(101, "Database error")).when(cardService).transferAmount(any(TransferRequest.class));
            mockMvc.perform(post("/api/cards/transfer")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new TransferRequest())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(101))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.message", Matchers.containsString("Database error")))
                    .andExpect(jsonPath("$.path").value("/api/cards/transfer"))
                    .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()));
        }
    }

    @Nested
    @DisplayName("GET /api/users/balance/{id}/{cardId}")
    class GetUserBalance {

        @Test
        @DisplayName("Позитивный сценарий")
        @WithMockUser(roles = "USER")
        void success() throws Exception {
            when(cardService.getBalance(userId, cardId)).thenReturn(BigDecimal.valueOf(1050));

            mockMvc.perform(get("/api/cards/balance/" + userId + "/" + cardId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(BigDecimal.valueOf(1050).toString()));

            verify(cardService, times(1)).getBalance(userId, cardId);
        }

        @Test
        @DisplayName("Не авторизированный пользователь")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/cards/balance/" + userId + "/" + cardId)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            verify(cardService, never()).getBalance(userId, cardId);
        }

        @Test
        @DisplayName("Ошибка в сервисном слое")
        @WithMockUser(roles = "USER")
        void exception() throws Exception {
            doThrow(new CardOperationException(101, "Database error")).when(cardService).getBalance(userId, cardId);

            mockMvc.perform(get("/api/cards/balance/" + userId + "/" + cardId)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(101))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.message", Matchers.containsString("Database error")))
                    .andExpect(jsonPath("$.path").value("/api/cards/balance/" + userId + "/" + cardId))
                    .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()));
        }
    }

}
