package com.example.bankcards.exception;


/**
 * Исключение при операциях с пользователями.
 */
public class UserOperationException extends BankCardException {

    public UserOperationException(int code, String message) {
        super(code, message);
    }

    @Override
    public String getMessage() {
        return String.format("User operation error: %s", super.getMessage());
    }
}
