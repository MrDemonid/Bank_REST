package com.example.bankcards.config;

import com.example.bankcards.util.CardDateUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.log4j.Log4j2;

import java.time.YearMonth;


/**
 * Конвертер YearMonth в строку и обратно, поскольку Spring сам этого не умеет.
 */
@Converter(autoApply = true)
@Log4j2
public class YearMonthToStringConverter implements AttributeConverter<YearMonth, String> {

    @Override
    public String convertToDatabaseColumn(YearMonth cardDate) {
        return CardDateUtil.toDbString(cardDate);
    }

    @Override
    public YearMonth convertToEntityAttribute(String strDate) {
        YearMonth yearMonth = CardDateUtil.toYearMonth(strDate);
        if (yearMonth == null) {
            log.error("Can't convert date ({}) to YearMonth", strDate);
        }
        return yearMonth;
    }

}
