package com.example.bankcards.dto.filters;

import com.example.bankcards.entity.Card;
import com.example.bankcards.util.CardDateUtil;
import com.example.bankcards.util.CardNumberHasher;
import com.example.bankcards.util.CardUtil;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Фильтр на Criteria API для карт.
 */
public class CardSpecification {

    public static Specification<Card> filterBy(CardFilter cardFilter) {
        return ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (cardFilter.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), cardFilter.getUserId()));
            }

            if (cardFilter.getCardNumber() != null) {
                String cardNumber = CardNumberHasher.hmacSha256(CardUtil.normalizeCardNumber(cardFilter.getCardNumber()));
                if (cardNumber != null) {
                    predicates.add(criteriaBuilder.equal(root.get("cardNumberHmac"), cardNumber));
                }
            }

            if (cardFilter.getCardStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), cardFilter.getCardStatus()));
            }

            if (cardFilter.getExpiryDate() != null) {
                System.out.println("Filter date: " + cardFilter.getExpiryDate().toString());
                String expiryStr = CardDateUtil.toDbString(cardFilter.getExpiryDate());
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("expiryDate"), expiryStr));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }
}
