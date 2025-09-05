package com.example.bankcards.exception;


/**
 * Исключение при операциях с ролями.
 */
public class IllegalRoleException extends BankCardException {

    public IllegalRoleException(int code, String message) {
        super(code, message);
    }

    @Override
    public String getMessage() {
        return String.format("Role operation error: %s.", super.getMessage());
    }
}
