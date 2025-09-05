package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Шифрование строки (номера карты) по алгоритму HMAC-SHA256.
 * Выдает стабильный хэш, пригодный для поиска карты в БД по её номеру.
 */
@Component
public class CardNumberHasher {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static String hmacSecret;

    public CardNumberHasher(@Value("${var.card.hmac.secret}") String secret) {
        CardNumberHasher.hmacSecret = secret;
    }

    public static String hmacSha256(String cardNumber) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(hmacSecret.getBytes(), HMAC_ALGO);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(cardNumber.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка при вычислении HMAC-SHA256", e);
        }
    }
}
