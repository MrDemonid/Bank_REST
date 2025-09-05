package com.example.bankcards.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * Класс для возврата ошибки.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Класс для возврата ошибки выполнения API")
public class ErrorResponse {
    @Schema(description = "Время возникновения ошибки", example = "2025-08-29T22:50:43.284262900")
    private LocalDateTime timestamp;

    @Schema(description = "Конкретизирующий код ошибки (из BankCardErrorCodes)", example = "1")
    private int errorCode;

    @Schema(description = "Код ошибки", example = "400")
    private int status;

    @Schema(description = "Тип ошибки", example = "BAD_REQUEST")
    private String error;

    @Schema(description = "Описание ошибки", example = "Ошибка операции с картой: card does not exist")
    private String message;

    @Schema(description = "Эндпоинт", example = "/api/cards/create")
    private String path;
}
