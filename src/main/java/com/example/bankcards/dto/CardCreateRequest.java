package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

/**
 * Запрос на добавление новой карты пользователю.
 */
@Schema(description = "Запрос на создание новой карты.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardCreateRequest {
    @Schema(description = "ID пользователя", example = "fb705a45-004d-4d60-989f-daa3e8572c49")
    private UUID userId;

    @Schema(description = "Номер карты", examples = {"1111222233334444", "1111-2222-3333-4444", "1111 2222 3333 4444"})
    private String cardNumber;

    @Schema(description = "До какой даты годна", example = "2028-08")
    private YearMonth expiryDate;

    @Schema(description = "Начальный капитал на карте", example = "100.50")
    private BigDecimal balance;
}
