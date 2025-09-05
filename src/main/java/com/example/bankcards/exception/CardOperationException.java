package com.example.bankcards.exception;

/**
 * Исключение при операциях с картами.
 */
public class CardOperationException extends BankCardException {

    public CardOperationException(int code, String message) {
        super(code, message);
    }

    @Override
    public String getMessage() {
        return String.format("Card operation error: %s.", super.getMessage());
    }
}
