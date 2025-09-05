package com.example.bankcards.repository;

import com.example.bankcards.entity.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Работа с БД ролями.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Возвращает роль по её имени.
     * @param name Имя роли (например "ROLE_USER")
     */
    Optional<Role> findByName(String name);

    /**
     * Возвращает множество ролей по их именам.
     * @param names Множество имён ролей.
     */
    Set<Role> findByNameIn(Collection<String> names);
}
