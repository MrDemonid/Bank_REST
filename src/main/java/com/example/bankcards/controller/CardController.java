package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.PageDTO;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.filters.CardFilter;
import com.example.bankcards.exception.ErrorResponse;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;


/**
 * Контроллер управления операциями с картами пользователей.
 */
@Tag(name = "Card controller", description = "REST API для управления банковскими картами.")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Некорректные данные или ошибка сервиса", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован.", content = @Content(schema = @Schema())),
        @ApiResponse(responseCode = "403", description = "Нет прав доступа", content = @Content(schema = @Schema()))
})
@RestController
@AllArgsConstructor
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;


    @Operation(summary = "Создание новой карты", description = "Создает новую карту и добавляет в базу данных. Только для роли ADMIN.")
    @ApiResponse(responseCode = "200", description = "Карта успешно создана")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<CardResponse> createCard(@RequestBody CardCreateRequest request) {
        return ResponseEntity.ok(cardService.createCard(request));
    }

    @Operation(summary = "Активация карты", description = "Активирует новую карту, меняя её статус в базе данных. Только для роли ADMIN.")
    @ApiResponse(responseCode = "200", description = "Карта успешно активирована")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/activate/{userId}/{id}")
    public ResponseEntity<CardResponse> activateCard(@PathVariable UUID userId, @PathVariable Long id) {
        return ResponseEntity.ok(cardService.activateCard(userId, id));
    }

    @Operation(summary = "Блокировка карты", description = "Блокирует карту, меняя её статус в базе данных. Только для роли ADMIN.")
    @ApiResponse(responseCode = "200", description = "Карта успешно заблокирована")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/block/{userId}/{id}")
    public ResponseEntity<CardResponse> blockCard(@PathVariable UUID userId, @PathVariable Long id) {
        return ResponseEntity.ok(cardService.blockedCard(userId, id));
    }

    @Operation(summary = "Удаление карты", description = "Удаляет карту из базы данных. Только для роли ADMIN.")
    @ApiResponse(responseCode = "200", description = "Карта успешно удалена")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}/{id}")
    public ResponseEntity<?> deleteCard(@PathVariable UUID userId, @PathVariable Long id) {
        cardService.deleteCard(userId, id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Просмотр карт", description = "Просмотр всех карт из базы данных, используя фильтр и пагинацию. Только для роли ADMIN.")
    @ApiResponse(responseCode = "200", description = "Список карт успешно получен")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/get-all")
    public ResponseEntity<PageDTO<CardResponse>> getAllCards(@RequestBody CardFilter filter, Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllCards(filter, pageable));
    }

    @Operation(summary = "Просмотр карт пользователя", description = "Просмотр пользователем только своих карт из базы данных, используя фильтр и пагинацию. Для роли USER.")
    @ApiResponse(responseCode = "200", description = "Список карт успешно получен")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/get-cards/{id}")
    public ResponseEntity<PageDTO<CardResponse>> getUserCards(@PathVariable UUID id, @RequestBody CardFilter filter, Pageable pageable) {
        return ResponseEntity.ok(cardService.getUserCards(id, filter, pageable));
    }

    @Operation(summary = "Запрос на блокировку карты", description = "Запрос пользователя на блокировку его карты. Запрос будет направлен в отдельную базу данных, для дальнейшего рассмотрения администратором.. Для роли USER.")
    @ApiResponse(responseCode = "200", description = "Запрос на блокировку успешно оформлен")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/request-block/{userId}/{cardId}")
    public ResponseEntity<?> requestBlockCard(@PathVariable UUID userId, @PathVariable Long cardId) {
        cardService.requestToBlockingCard(userId, cardId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Перевод средств с карты на карту", description = "Перевод средств между картами пользователя.. Для роли USER.")
    @ApiResponse(responseCode = "200", description = "Средства успешно переведены")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/transfer")
    public ResponseEntity<?> transferCard(@RequestBody TransferRequest request) {
        cardService.transferAmount(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Запрос баланса", description = "Запрос баланса карты пользователя.. Для роли USER.")
    @ApiResponse(responseCode = "200", description = "Возвращен баланс карты")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/balance/{id}/{cardId}")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable UUID id, @PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.getBalance(id, cardId));
    }


}
