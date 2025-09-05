package com.example.bankcards.controller;

import com.example.bankcards.BaseCardControllerTest;
import com.example.bankcards.dto.*;
import com.example.bankcards.dto.filters.CardFilter;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.CardOperationException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class CardControllerAdminTest extends BaseCardControllerTest {

    @Nested
    @DisplayName("POST /api/cards/create")
    class CreateCard {

        @Test
        @DisplayName("Позитивный сценарий")
        @WithMockUser(roles = "ADMIN")
        void success() throws Exception {
            CardCreateRequest request = new CardCreateRequest(userId, cardNumber, expiryDate, new BigDecimal("100.50"));
            when(cardService.createCard(request)).thenReturn(cardResponse);

            // выполняем и проверяем
            mockMvc.perform(post("/api/cards/create")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.cardNumber", is(cardHideNumber)))
                    .andExpect(jsonPath("$.expiryDate", is(expiryDate.toString())))
                    .andExpect(jsonPath("$.status", is(CardStatus.ACTIVE.toString())))
                    .andExpect(jsonPath("$.userId", is(userId.toString())))
                    .andExpect(jsonPath("$.balance").value(BigDecimal.ZERO));

            verify(cardService, times(1)).createCard(request);
        }

        @Test
        @DisplayName("Недостаточно прав")
        @WithMockUser(roles = "USER")
        void forbidden() throws Exception {
            // выполняем и проверяем
            mockMvc.perform(post("/api/cards/create")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CardCreateRequest())))
                    .andExpect(status().isForbidden());
            verify(cardService, never()).createCard(any(CardCreateRequest.class));
        }

        @Test
        @DisplayName("Ошибка в сервисном слое")
        @WithMockUser(roles = "ADMIN")
        void exception() throws Exception {
            when(cardService.createCard(any(CardCreateRequest.class))).thenThrow(new CardOperationException(101, "Database error"));
            // выполняем и проверяем
            mockMvc.perform(post("/api/cards/create")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CardCreateRequest())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(101))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.message", Matchers.containsString("Database error")))
                    .andExpect(jsonPath("$.path").value("/api/cards/create"))
                    .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()));
        }
    }


    @Nested
    @DisplayName("PUT /api/cards/activate/{userId}/{cardId}")
    class ActivateCard {

        @Test
        @DisplayName("Позитивный сценарий")
        @WithMockUser(roles = "ADMIN")
        void success() throws Exception {
            when(cardService.activateCard(userId, cardId)).thenReturn(cardResponse);

            // выполняем и проверяем
            mockMvc.perform(put("/api/cards/activate/" + userId + "/" + cardId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.cardNumber", is(cardHideNumber)))
                    .andExpect(jsonPath("$.expiryDate", is(expiryDate.toString())))
                    .andExpect(jsonPath("$.status", is(CardStatus.ACTIVE.toString())))
                    .andExpect(jsonPath("$.userId", is(userId.toString())))
                    .andExpect(jsonPath("$.balance").value(BigDecimal.ZERO));
            verify(cardService, times(1)).activateCard(userId, cardId);
        }

        @Test
        @DisplayName("Недостаточно прав")
        @WithMockUser(roles = "USER")
        void forbidden() throws Exception {
            // выполняем и проверяем
            mockMvc.perform(put("/api/cards/activate/" + userId + "/" + cardId)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
            verify(cardService, never()).activateCard(userId, cardId);
        }

        @Test
        @DisplayName("Ошибка в сервисном слое")
        @WithMockUser(roles = "ADMIN")
        void exception() throws Exception {
            when(cardService.activateCard(userId, cardId)).thenThrow(new CardOperationException(101, "Database error"));
            // выполняем и проверяем
            mockMvc.perform(put("/api/cards/activate/" + userId + "/" + cardId)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(101))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.message", Matchers.containsString("Database error")))
                    .andExpect(jsonPath("$.path").value("/api/cards/activate/" + userId + "/" + cardId))
                    .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()));
        }
    }

    @Nested
    @DisplayName("PUT /api/cards/block/{userId}/{cardId}")
    class BlockCard {

        @Test
        @DisplayName("Позитивный сценарий")
        @WithMockUser(roles = "ADMIN")
        void success() throws Exception {
            when(cardService.blockedCard(userId, cardId)).thenReturn(cardResponse);

            // выполняем и проверяем
            mockMvc.perform(put("/api/cards/block/" + userId + "/" + cardId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.cardNumber", is(cardHideNumber)))
                    .andExpect(jsonPath("$.expiryDate", is(expiryDate.toString())))
                    .andExpect(jsonPath("$.status", is(CardStatus.ACTIVE.toString())))
                    .andExpect(jsonPath("$.userId", is(userId.toString())))
                    .andExpect(jsonPath("$.balance").value(BigDecimal.ZERO));
            verify(cardService, times(1)).blockedCard(userId, cardId);
        }

        @Test
        @DisplayName("Недостаточно прав")
        @WithMockUser(roles = "USER")
        void forbidden() throws Exception {
            // выполняем и проверяем
            mockMvc.perform(put("/api/cards/block/" + userId + "/" + cardId)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
            verify(cardService, never()).blockedCard(userId, cardId);
        }

        @Test
        @DisplayName("Ошибка в сервисном слое")
        @WithMockUser(roles = "ADMIN")
        void exception() throws Exception {
            when(cardService.blockedCard(userId, cardId)).thenThrow(new CardOperationException(101, "Database error"));
            // выполняем и проверяем
            mockMvc.perform(put("/api/cards/block/" + userId + "/" + cardId)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(101))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.message", Matchers.containsString("Database error")))
                    .andExpect(jsonPath("$.path").value("/api/cards/block/" + userId + "/" + cardId))
                    .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()));
        }
    }

    @Nested
    @DisplayName("DELETE /api/cards/{userId}/{cardId}")
    class DeleteCard {

        @Test
        @DisplayName("Позитивный сценарий")
        @WithMockUser(roles = "ADMIN")
        void success() throws Exception {
            doNothing().when(cardService).deleteCard(userId, cardId);

            // выполняем и проверяем
            mockMvc.perform(delete("/api/cards/" + userId + "/" + cardId)
                            .with(csrf()))
                    .andExpect(status().isOk());
            verify(cardService, times(1)).deleteCard(userId, cardId);
        }

        @Test
        @DisplayName("Недостаточно прав")
        @WithMockUser(roles = "USER")
        void forbidden() throws Exception {
            // выполняем и проверяем
            mockMvc.perform(delete("/api/cards/" + userId + "/" + cardId)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
            verify(cardService, never()).deleteCard(userId, cardId);
        }

        @Test
        @DisplayName("Ошибка в сервисном слое")
        @WithMockUser(roles = "ADMIN")
        void exception() throws Exception {
            doThrow(new CardOperationException(101, "Database error")).when(cardService).deleteCard(userId, cardId);
            // выполняем и проверяем
            mockMvc.perform(delete("/api/cards/" + userId + "/" + cardId)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(101))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.message", Matchers.containsString("Database error")))
                    .andExpect(jsonPath("$.path").value("/api/cards/" + userId + "/" + cardId))
                    .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()));
        }
    }

    @Nested
    @DisplayName("POST /api/cards/get-all")
    class GetAllCards {

        @Test
        @DisplayName("Позитивный сценарий")
        @WithMockUser(roles = "ADMIN")
        void success() throws Exception {
            PageDTO<CardResponse> response = new PageDTO<>(List.of(cardResponse), 1, 1, 10, 0);
            CardFilter filter = new CardFilter(null, null, CardStatus.ACTIVE, null);

            when(cardService.getAllCards(any(CardFilter.class), any(Pageable.class))).thenReturn(response);

            // выполняем и проверяем
            mockMvc.perform(post("/api/cards/get-all")
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

            verify(cardService, times(1)).getAllCards(any(CardFilter.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Недостаточно прав")
        @WithMockUser(roles = "USER")
        void forbidden() throws Exception {
            mockMvc.perform(post("/api/cards/get-all")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CardFilter())))
                    .andExpect(status().isForbidden());
            verify(cardService, never()).getAllCards(any(CardFilter.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Пустой результат")
        @WithMockUser(roles = "ADMIN")
        void emptyResult() throws Exception {
            when(cardService.getAllCards(any(CardFilter.class), any(Pageable.class)))
                    .thenReturn(PageDTO.empty());

            mockMvc.perform(post("/api/cards/get-all")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CardFilter())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalPages").value(0))
                    .andExpect(jsonPath("$.totalElements").value(0));
            verify(cardService, times(1)).getAllCards(any(CardFilter.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Ошибка в сервисном слое")
        @WithMockUser(roles = "ADMIN")
        void exception() throws Exception {
            doThrow(new CardOperationException(101, "Database error")).when(cardService).getAllCards(any(CardFilter.class), any(Pageable.class));

            // выполняем и проверяем
            mockMvc.perform(post("/api/cards/get-all")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CardFilter())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(101))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.message", Matchers.containsString("Database error")))
                    .andExpect(jsonPath("$.path").value("/api/cards/get-all"))
                    .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()));
        }
    }


}
