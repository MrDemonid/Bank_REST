package com.example.bankcards.service.validators;

import com.example.bankcards.entity.auth.Role;
import com.example.bankcards.exception.BankCardErrorCodes;
import com.example.bankcards.exception.IllegalRoleException;
import com.example.bankcards.repository.RoleRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Валидатор для ролей.
 */
@Component
@AllArgsConstructor
@Log4j2
public class RoleValidator {

    private final RoleRepository roleRepository;

    /**
     * Проверка списка имен ролей на корректность и соответствие ролям в БД.
     * @param roleNames Список имен ролей.
     */
    public void validateRoles(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            log.error("Roles is null or empty");
            throw new IllegalRoleException(BankCardErrorCodes.ROLE_ILLEGAL, "roles name is empty");
        }
        Set<String> normalized = roleNames.stream()
                .map(name -> name.startsWith("ROLE_") ? name: "ROLE_" + name)
                .collect(Collectors.toSet());

        Set<Role> roles = roleRepository.findByNameIn(normalized);

        if (roles.size() != roleNames.size()) {
            // ищем каких ролей не оказалось в БД (для включения в отчет об ошибке)
            Set<String> found = roles.stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
            Set<String> missing = new HashSet<>(normalized);
            missing.removeAll(found);
            log.error("Roles not found: {}", missing);
            throw new IllegalRoleException(BankCardErrorCodes.ROLE_NOT_FOUND, "roles not found: " + missing);
        }
    }

}
