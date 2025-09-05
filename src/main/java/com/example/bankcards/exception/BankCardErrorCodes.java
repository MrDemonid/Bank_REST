package com.example.bankcards.exception;


/**
 * Коды ошибок.
 */
public class BankCardErrorCodes {

    public static final int UNKNOWN_ERROR_CODE = 90000;

    public static final int CARD_NOT_FOUND = 10001;
    public static final int CARD_ALREADY_EXISTS = 10002;
    public static final int CARD_INVALID_NUMBER = 10003;
    public static final int CARD_INVALID_EXPIRATION = 10004;
    public static final int CARD_INVALID_OWNER = 10005;
    public static final int CARD_ALREADY_BLOCKED = 10006;
    public static final int CARD_CANNOT_BE_SAME = 10007;
    public static final int CARD_BAD_STATUS = 10008;
    public static final int CARD_NOT_ENOUGHT_FUNDS = 10009;

    public static final int USER_NAME_IS_WRONG = 20003;
    public static final int USER_NOT_FOUND = 20004;
    public static final int USER_ALREADY_EXISTS = 20005;
    public static final int USER_PASSWORD_IS_WRONG = 20006;
    public static final int USER_EMAIL_IS_WRONG = 20007;

    public static final int ROLE_NOT_FOUND = 30005;
    public static final int ROLE_ILLEGAL = 30006;

}
