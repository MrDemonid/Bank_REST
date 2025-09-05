package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Запрос пользователя на перевод средств между своими картами.
 */
@Schema(description = "Запрос на перевод средств с карты на карту.")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    @Schema(description = "ID пользователя", example = "fb705a45-004d-4d60-989f-daa3e8572c49")
    private UUID userId;

    @Schema(description = "ID карты-источника")
    private Long fromCardId;

    @Schema(description = "ID карты-приемника")
    private Long toCardId;

    @Schema(description = "Количество переводимых средств", example = "100.50")
    private BigDecimal amount;
}

