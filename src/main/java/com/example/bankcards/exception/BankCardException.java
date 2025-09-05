package com.example.bankcards.exception;

/**
 * Базовый класс исключений.
 */
public abstract class BankCardException extends RuntimeException {
    private final int code;

    public BankCardException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
