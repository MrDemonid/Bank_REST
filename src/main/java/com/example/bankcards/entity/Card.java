package com.example.bankcards.entity;

import com.example.bankcards.config.CardNumberEncryptor;
import com.example.bankcards.config.YearMonthToStringConverter;
import com.example.bankcards.entity.auth.User;
import com.example.bankcards.util.CardNumberHasher;
import com.example.bankcards.util.CardUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;


/**
 * Банковская карта.
 * Вместо прямого связывания с User через @ManyToOne, используется
 * простое хранение UUID владельца карты. Это обеспечивает больше
 * гибкости, в случае отделения Auth-сервера в отдельный микросервис,
 * или при переходе на сторонний (тот же Keycloak).
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Log4j2
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Номер карты. Хранится в зашифрованном виде (AES).
     * Конвертер обеспечит прозрачное шифрование/дешифрование.
     */
    @Column(nullable = false, unique = true)
    @Convert(converter = CardNumberEncryptor.class)
    private String cardNumber;

    /**
     * HMAC от номера карты — для поиска и проверки уникальности.
     */
    @Column(name = "card_number_hmac", nullable = false, unique = true, length = 64)
    private String cardNumberHmac;

    /**
     * Дата валидности карты. В БД храним в виде строки VARCHAR(5).
     * Поскольку Jpa не поддерживает YearMonth, то задаем конвертер для
     * автоматического преобразования в строку и обратно.
     */
    @Convert(converter = YearMonthToStringConverter.class)
    @Column(nullable = false, length = 7)
    private YearMonth expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;


    /*
        Обеспечиваем уникальность для каждого объекта.
        А поскольку номер карты уникален, то учитываем только его.
    */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Card card = (Card) o;
        return cardNumberHmac.equals(card.cardNumberHmac);
    }

    @Override
    public int hashCode() {
        return cardNumberHmac.hashCode();
    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", cardNumber='" + CardUtil.getMaskedNumber(cardNumber) + '\'' +
                ", expiryDate=" + expiryDate +
                ", status=" + status +
                ", balance=" + balance +
                ", user=" + userId +
                '}';
    }
}
