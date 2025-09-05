package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Работа с БД карт.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {

    /**
     * Проверка наличия карты в БД по её хэшу.
     * @param cardNumberHmac Хэш номера карты.
     */
    boolean existsByCardNumberHmac(String cardNumberHmac);

}
