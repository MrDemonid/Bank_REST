package com.example.bankcards.util;

import lombok.extern.log4j.Log4j2;

import java.time.YearMonth;


@Log4j2
public class CardDateUtil {

    /**
     * Преобразует YearMonth в строку для хранения/сравнения в БД
     */
    public static String toDbString(YearMonth yearMonth) {
        if (yearMonth == null)
            return null;
        return String.format("%04d/%02d", yearMonth.getYear(), yearMonth.getMonthValue());
    }

    /**
     * Преобразует строку из БД в YearMonth
     * @param dbString Дата в строковом формате "2028/08"
     */
    public static YearMonth toYearMonth(String dbString) {
        if (dbString == null || !dbString.matches("^\\d{4}/(0[1-9]|1[0-2])$")) {
            return null;
        }
        String[] parts = dbString.split("/");
        try {
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            return YearMonth.of(year, month);
        } catch (NumberFormatException e) {
            log.error("Error parsing String('{}') to YearMonth", dbString);
            return null;
        }
    }
}