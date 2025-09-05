package com.example.bankcards.repository;

import com.example.bankcards.entity.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Работа с БД юзеров.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    /**
     * Проверка существования пользователя по кго ID.
     */
    Boolean existsUserById(UUID id);

    /**
     * Возвращает данные пользователя по его имени.
     * @param username Имя пользователя.
     */
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsername(String username);
}
