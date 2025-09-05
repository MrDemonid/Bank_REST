package com.example.bankcards.service.validators;

import com.example.bankcards.dto.SignUpRequest;
import com.example.bankcards.exception.BankCardErrorCodes;
import com.example.bankcards.exception.BankCardException;
import com.example.bankcards.exception.UserOperationException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * Валидатор данных пользователя.
 * Поскольку в ТЗ ничего нет о проверке надежности пароля, корректности почты и тд., то
 * просто обозначил проверки в учебных целях.
 */
@Component
@AllArgsConstructor
@Log4j2
public class UserValidator {
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 64;

    private final RoleValidator roleValidator;


    /**
     * Проверка корректности данных для создания нового пользователя.
     * @param request Структура с данными о пользователе.
     */
    public void validateUserRequest(SignUpRequest request) throws BankCardException {
        if (!checkUserName(request.getUserName())) {
            throw new UserOperationException(BankCardErrorCodes.USER_NAME_IS_WRONG, "username is wrong");
        }
        if (!checkPassword(request.getPassword())) {
            throw new UserOperationException(BankCardErrorCodes.USER_PASSWORD_IS_WRONG, "password is wrong");
        }
        if (!checkEmail(request.getEmail())) {
            throw new UserOperationException(BankCardErrorCodes.USER_EMAIL_IS_WRONG, "email is wrong");
        }
        roleValidator.validateRoles(request.getRoles());
    }

    private boolean checkUserName(String userName) {
        return userName != null && !userName.trim().isEmpty();
    }

    private boolean checkPassword(String password) {
        return password != null && !password.trim().isEmpty() && password.length() >= MIN_PASSWORD_LENGTH && password.length() <= MAX_PASSWORD_LENGTH;
    }

    private boolean checkEmail(String email) {
        if (email == null)
            return false;

        // пробелы/табуляции не допускаем
        email = email.trim();
        if (email.isEmpty() || email.chars().anyMatch(Character::isWhitespace)) {
            return false;
        }

        // ровно одна @ и не в начале
        int at = email.indexOf('@');
        if (at <= 0 || at != email.lastIndexOf('@')) {
            return false;
        }

        // точка должна быть в домене, не первой и не последней
        String domain = email.substring(at + 1);
        int dot = domain.indexOf('.');
        if (dot <= 0 || dot == domain.length() - 1) {
            return false;
        }

        // доп. проверка: в домене нет пустых меток вида "mail..ru" или ".ru"
        String[] labels = domain.split("\\.");
        for (String label : labels) {
            if (label.isEmpty()) return false;
        }

        return true;
    }
}
