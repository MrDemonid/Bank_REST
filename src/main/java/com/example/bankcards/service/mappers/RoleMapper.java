package com.example.bankcards.service.mappers;

import com.example.bankcards.entity.auth.Role;
import com.example.bankcards.repository.RoleRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Конвертер для сущности Role.
 */
@Component
@AllArgsConstructor
@Log4j2
public class RoleMapper {
    private final RoleRepository roleRepository;


    /**
     * Конвертер списка названий ролей в список сущностей ролей.
     * @param roleNames Валидный список названий ролей.
     * @return Список сущностей ролей.
     */
    public Set<Role> toRoles(Set<String> roleNames) {
        Set<String> normalized = roleNames.stream()
                .map(name -> name.startsWith("ROLE_") ? name: "ROLE_" + name)
                .collect(Collectors.toSet());

        return roleRepository.findByNameIn(normalized);
    }

}
