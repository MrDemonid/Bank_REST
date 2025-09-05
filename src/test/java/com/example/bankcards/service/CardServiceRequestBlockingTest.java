package com.example.bankcards.service;

import com.example.bankcards.BaseCardServiceTest;
import com.example.bankcards.entity.BlockedCards;
import com.example.bankcards.entity.CardAction;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.repository.BlockedCardsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CardServiceRequestBlockingTest extends BaseCardServiceTest {

    @Mock
    protected BlockedCardsRepository blockedCardsRepository;

    protected Long cardId;


    @BeforeEach
    public void setup() {
        super.setup();
        cardId = 1L;
    }

    /*
            Тест с нормальными параметрами.
         */
    @Test
    void cardRequestBlocking_success() {
        // моки
        when(userService.existsUser(userId)).thenReturn(true);
        when(blockedCardsRepository.existsBlockedCardsByCardId(cardId)).thenReturn(false);
        when(cardRepository.findById(cardId)).thenReturn(Optional.ofNullable(card));

        // выполняем
        assertDoesNotThrow(() -> cardService.requestToBlockingCard(userId, cardId));

        // проверяем
        ArgumentCaptor<BlockedCards> captor = ArgumentCaptor.forClass(BlockedCards.class);
        verify(blockedCardsRepository).save(captor.capture());
        // должны сохранить ID карты и код операции - BLOCKED
        BlockedCards saved = captor.getValue();
        assertEquals(cardId, saved.getCardId());
        assertEquals(CardAction.BLOCK, saved.getAction());
    }

    /*
        Тест на неверного юзера.
     */
    @Test
    void cardRequestBlocking_userNotFound() {
        when(userService.existsUser(userId)).thenReturn(false);

        assertThrows(CardOperationException.class, () -> cardService.requestToBlockingCard(userId, cardId));

        verify(blockedCardsRepository, never()).save(any(BlockedCards.class));
    }

    /*
        Тест на уже существующий запрос.
     */
    @Test
    void cardRequestBlocking_cardAlreadyExists() {
        when(userService.existsUser(userId)).thenReturn(true);
        when(blockedCardsRepository.existsBlockedCardsByCardId(cardId)).thenReturn(true);

        assertThrows(CardOperationException.class, () -> cardService.requestToBlockingCard(userId, cardId));

        verify(blockedCardsRepository, never()).save(any(BlockedCards.class));
    }

    /*
        Тест на отсутствие карты в БД.
     */
    @Test
    void cardRequestBlocking_cardNotFound() {
        when(userService.existsUser(userId)).thenReturn(true);
        when(blockedCardsRepository.existsBlockedCardsByCardId(cardId)).thenReturn(false);
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardOperationException.class, () -> cardService.requestToBlockingCard(userId, cardId));

        verify(blockedCardsRepository, never()).save(any(BlockedCards.class));
    }

    /*
        Тест на принадлежность карты другому пользователю.
     */
    @Test
    void cardRequestBlocking_cardBelongToAnotherUser() {
        when(userService.existsUser(userId)).thenReturn(true);
        when(blockedCardsRepository.existsBlockedCardsByCardId(cardId)).thenReturn(false);
        when(cardRepository.findById(cardId)).thenReturn(Optional.ofNullable(card));
        card.setUserId(UUID.randomUUID());

        assertThrows(CardOperationException.class, () -> cardService.requestToBlockingCard(userId, cardId));

        verify(blockedCardsRepository, never()).save(any(BlockedCards.class));
    }

}
