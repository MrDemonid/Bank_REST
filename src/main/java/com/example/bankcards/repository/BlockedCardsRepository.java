package com.example.bankcards.repository;

import com.example.bankcards.entity.BlockedCards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * БД для заявок пользователей на блокировку карт)
 */
@Repository
public interface BlockedCardsRepository extends JpaRepository<BlockedCards, Long> {

    /**
     * Проверка
     * @param cardId Уникальный идентификатор карты.
     */
    boolean existsBlockedCardsByCardId(Long cardId);
}
