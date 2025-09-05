package com.example.bankcards.dto.filters;

import com.example.bankcards.entity.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.util.UUID;

/**
 * Фильтр для отбора карт.
 */
@Schema(description = "Фильтр для выборки карт из БД по заданным параметрам. Если поле равно null, то он не учитывается.")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardFilter {
    @Schema(description = "Идентификатор пользователя", example = "e493a2f3-f1ea-4041-a3d7-28bc88b2f423")
    private UUID userId;

    @Schema(description = "Полный номер карты пользователя", examples = {"1111 2222 3333 4444", "1111222233334444", "1111-2222-3333-4444"})
    private String cardNumber;

    @Schema(description = "Искать по статусу карты", example = "BLOCKED")
    private CardStatus cardStatus;

    @Schema(description = "Искать карты действительные до заданной даты", example = "2028-06")
    private YearMonth expiryDate;
}
