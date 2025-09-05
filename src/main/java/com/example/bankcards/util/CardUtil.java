package com.example.bankcards.util;


import java.time.YearMonth;

/**
 * Утилиты для удобной работы с номерами карт.
 */
public class CardUtil {

    /**
     * Валидация номера карты.
     * @param cardNumber Номер карты, с любым разделением номеров (1111-2222..., 11112222..., etc.)
     */
    public static boolean isCardNumberValid(String cardNumber) {
        String number = normalizeCardNumber(cardNumber == null ? "" : cardNumber.trim());
        if (number.length() != 19) {
            return false;
        }
        return number.matches("^\\d{4} \\d{4} \\d{4} \\d{4}$");
    }

    /**
     * Валидация даты действия карты.
     * @param cardExpiry Дата, до которой карта валидна.
     */
    public static boolean isCardExpiryValid(YearMonth cardExpiry) {
        return cardExpiry != null && YearMonth.now().isBefore(cardExpiry);
    }

    /**
     * Нормализация формата карты.
     * @param cardNumber Номер карты, с любым разделением номеров (1111-2222..., 11112222..., etc.)
     * @return Номер карты, вида "1111 2222 3333 4444"
     */
    public static String normalizeCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 16) {
            return "";
        }
        // убираем все лишние символы и пробелы
        String card = cardNumber.replaceAll("\\D", "");
        if (card.length() != 16) {
            return "";
        }
        // разбиваем по 4 цифры с пробелом
        return card.replaceAll("(.{4})(?!$)", "$1 ");
    }

    /**
     * Возвращает замаскированный номер карты.
     * @param cardNumber Номер карты в любом формате.
     * @return Номер карты в виде "**** **** **** NNNN", или "???? ???? ???? ????" в случае не валидного номера карты.
     */
    public static String getMaskedNumber(String cardNumber) {
        String number = CardUtil.normalizeCardNumber(cardNumber);
        if (CardUtil.isCardNumberValid(number)) {
            return "**** **** **** " + number.substring(number.length() - 4);
        }
        return "???? ???? ???? ????";
    }


}
