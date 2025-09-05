package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;


/**
 * Информация о карте. Возвращается из REST API.
 */
@Schema(description = "Информация по карте.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {
    @Schema(description = "ID карты", example = "1")
    private Long id;

    @Schema(description = "Номер карты", example = "**** **** **** 4444")
    private String cardNumber;

    @Schema(description = "До какой даты годна", example = "2028-08")
    private YearMonth expiryDate;

    @Schema(description = "Текущий статус", example = "ACTIVE")
    private CardStatus status;

    @Schema(description = "Средства на карте", example = "100.00")
    private BigDecimal balance;

    @Schema(description = "ID пользователя", example = "fb705a45-004d-4d60-989f-daa3e8572c49")
    private UUID userId;
}
