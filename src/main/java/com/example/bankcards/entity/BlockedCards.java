package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;

/**
 * Запросы от пользователя на действия с их картами.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Log4j2
public class BlockedCards {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardAction action;

    @Column(nullable = false)
    private LocalDateTime requestedDate;
}
