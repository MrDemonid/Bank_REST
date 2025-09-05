package com.example.bankcards.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Глобальная обработка ошибок.
 * Если словили ошибку из другого микросервиса (например ответ от Feign-запроса),
 * то просто пробрасываем её дальше (опознаем ситуацию по пришедшему к нам JSON-описанию ошибки).
 * На выходе: структура ErrorResponse.
 */
@RestControllerAdvice
@Log4j2
public class ExceptionController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(BankCardException.class)
    public ResponseEntity<ErrorResponse> cardException(BankCardException e, HttpServletRequest request) {
        String message = e.getMessage();

        // Попытка распарсить message как ErrorResponse
        if (message != null && message.trim().startsWith("{")) {
            try {
                ErrorResponse nested = objectMapper.readValue(message, ErrorResponse.class);
                log.error("Проброс ошибки из предыдущего сервиса: {}", nested);
                return new ResponseEntity<>(nested, HttpStatus.valueOf(nested.getStatus()));
            } catch (Exception ex) {
                log.warn("Не удалось распарсить message как ErrorResponse: {}", message);
            }
        }

        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                e.getCode(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                message,
                request.getRequestURI()
        );
        log.error("Ошибка: {}", body);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

}
