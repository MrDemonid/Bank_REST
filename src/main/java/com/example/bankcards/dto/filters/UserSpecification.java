package com.example.bankcards.dto.filters;

import com.example.bankcards.entity.auth.Role;
import com.example.bankcards.entity.auth.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


/**
 * Фильтр на Criteria API для пользователей.
 */
public class UserSpecification {

    public static Specification<User> filterBy(UserFilter userFilter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userFilter.getUsername() != null) {
                // Используем "LIKE" для частичного совпадения в любом месте строки
                predicates.add(criteriaBuilder.like(root.get("username"), String.format("%%%s%%", userFilter.getUsername()))); // WHERE username = :%userName%
            }
            if (userFilter.getEmail() != null) {
                predicates.add(criteriaBuilder.like(root.get("email"), String.format("%%%s%%", userFilter.getEmail())));
            }
            if (userFilter.getEnabled() != null) {
                predicates.add(criteriaBuilder.equal(root.get("enabled"), userFilter.getEnabled()));
            }
            if (userFilter.getRole() != null) {
                Join<User, Role> rolesJoin = root.join("roles", JoinType.INNER);
                predicates.add(criteriaBuilder.like(rolesJoin.get("name"), String.format("%%%s%%", userFilter.getRole())));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
