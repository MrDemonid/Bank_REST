package com.example.bankcards.integrations;

import com.example.bankcards.BasicIntegrationTests;
import com.example.bankcards.dto.SignUpRequest;
import com.example.bankcards.dto.UpdateUserRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.dto.filters.UserFilter;
import com.example.bankcards.entity.auth.Role;
import com.example.bankcards.entity.auth.User;
import com.example.bankcards.exception.BankCardErrorCodes;
import com.example.bankcards.exception.UserOperationException;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;


class UserServiceIntegrationTest extends BasicIntegrationTests {

    @Autowired
    private UserService userService;

    private User defaultUser;


    @BeforeEach
    void setup() {
        // Создаем пользователя с ролями 'USER' и 'ADMIN'
        Set<Role> roles = new HashSet<>(roleRepository.findAll());
        // получаем ID для пользователя
        defaultUser = userRepository.save(new User(null, "Ivan", "12345678", "test@mail.com", true, new HashSet<>()));
        // теперь назначаем роли
        roles.forEach(defaultUser::addRole);
        userRepository.save(defaultUser);
    }


    @Nested
    @DisplayName("Создание пользователя")
    class CreateUser {
        @Test
        void success() {
            userRepository.deleteAll();
            Set<String> roles = Set.of("ROLE_USER", "ROLE_ADMIN");
            SignUpRequest request = new SignUpRequest("Andrey", "andrey@example.com", "12345678", roles);
            UserResponse response = userService.createUser(request);

            assertThat(response)
                    .as("Проверка создания пользователя")
                    .satisfies(user -> {
                        assertThat(user.getUserId()).as("У пользователя должен быть ID").isNotNull();
                        assertThat(user.getUserName()).as("Имя должно совпадать").isEqualTo("Andrey");
                        assertThat(user.getEmail()).as("Email должен совпадать").isEqualTo("andrey@example.com");
                        assertThat(user.isEnabled()).as("Пользователь должен быть активирован").isTrue();
                        assertThat(user.getRoles()).as("Должны быть назначены роли").hasSize(2).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
                    });
        }

        @Test
        void alreadyExists() {
            SignUpRequest request = new SignUpRequest("Ivan", "test@mail.com", "12345678", Set.of("USER", "ADMIN"));

            UserOperationException ex = assertThrows(UserOperationException.class, () -> userService.createUser(request));

            assertEquals(BankCardErrorCodes.USER_ALREADY_EXISTS, ex.getCode());
        }

        @Test
        void validateUsername() {
            SignUpRequest request = new SignUpRequest("", "test@mail.com", "12345678", Set.of("USER", "ADMIN"));

            UserOperationException ex = assertThrows(UserOperationException.class, () -> userService.createUser(request));

            assertEquals(BankCardErrorCodes.USER_NAME_IS_WRONG, ex.getCode());
        }

        @Test
        void validatePassword() {
            SignUpRequest request = new SignUpRequest("Sergey", "test@mail.com", "123", Set.of("USER", "ADMIN"));

            UserOperationException ex = assertThrows(UserOperationException.class, () -> userService.createUser(request));

            assertEquals(BankCardErrorCodes.USER_PASSWORD_IS_WRONG, ex.getCode());
        }

        @Test
        void validateEmail() {
            SignUpRequest request = new SignUpRequest("Sergey", "test@ivan@mail.ru", "12345678", Set.of("USER", "ADMIN"));

            UserOperationException ex = assertThrows(UserOperationException.class, () -> userService.createUser(request));

            assertEquals(BankCardErrorCodes.USER_EMAIL_IS_WRONG, ex.getCode());
        }

    }


    @Nested
    @DisplayName("Обновление данных пользователя")
    class UpdateUser {

        @Test
        void success() {
            UpdateUserRequest request = new UpdateUserRequest("Sergey", "new@mail.ru", "qwertyui", Set.of("ROLE_USER"), defaultUser.getId(), true);

            UserResponse response = userService.updateUser(request);

            assertThat(response)
                    .as("Проверка апдейта пользователя")
                    .satisfies(user -> {
                        assertThat(user.getUserId()).as("Неверный ID пользователя").isEqualTo(defaultUser.getId());
                        assertThat(user.getUserName()).as("Неверное имя пользователя").isEqualTo("Sergey");
                        assertThat(user.getEmail()).as("Неверный Email пользователя").isEqualTo("new@mail.ru");
                        assertThat(user.isEnabled()).as("Пользователь должен быть активирован").isTrue();
                        assertThat(user.getRoles()).as("Не совпадают роли").hasSize(1).containsExactlyInAnyOrder("ROLE_USER");
                    });
        }

        @Test
        void userNotFound() {
            UpdateUserRequest request = new UpdateUserRequest("Sergey", "new@mail.ru", "qwertyui", Set.of("ROLE_USER"), UUID.randomUUID(), true);

            UserOperationException ex = assertThrows(UserOperationException.class, () -> userService.updateUser(request));

            assertEquals(BankCardErrorCodes.USER_NOT_FOUND, ex.getCode());
        }

        @Test
        void validateUsername() {
            UpdateUserRequest request = new UpdateUserRequest("", "new@mail.ru", "qwertyui", Set.of("ROLE_USER"), defaultUser.getId(), true);

            UserOperationException ex = assertThrows(UserOperationException.class, () -> userService.updateUser(request));

            assertEquals(BankCardErrorCodes.USER_NAME_IS_WRONG, ex.getCode());
        }

        @Test
        void validatePassword() {
            UpdateUserRequest request = new UpdateUserRequest("Sergey", "new@mail.ru", "qwer", Set.of("ROLE_USER"), defaultUser.getId(), true);

            UserOperationException ex = assertThrows(UserOperationException.class, () -> userService.updateUser(request));

            assertEquals(BankCardErrorCodes.USER_PASSWORD_IS_WRONG, ex.getCode());
        }

        @Test
        void validateEmail() {
            UpdateUserRequest request = new UpdateUserRequest("Sergey", "test.ivan@mail.", "qwertyui", Set.of("ROLE_USER"), defaultUser.getId(), true);

            UserOperationException ex = assertThrows(UserOperationException.class, () -> userService.updateUser(request));

            assertEquals(BankCardErrorCodes.USER_EMAIL_IS_WRONG, ex.getCode());
        }
    }


    @Nested
    @DisplayName("Удаление пользователя")
    class DeleteUser {

        @Test
        void success() {
            userService.deleteUser(defaultUser.getId());

            assertThat(userRepository.existsById(defaultUser.getId())).isFalse();
        }

        @Test
        void userNotFound() {
            UserOperationException ex = assertThrows(UserOperationException.class, () -> userService.deleteUser(UUID.randomUUID()));

            assertThat(ex.getCode()).isEqualTo(BankCardErrorCodes.USER_NOT_FOUND);
        }
    }


    @Nested
    @DisplayName("Выборка пользователей")
    class GetAllUsers {

        private User twoUser;
        private User threeUser;

        @BeforeEach
        void setup() {
            // Создаем пользователя с ролями 'USER' и 'ADMIN'
            Role role = roleRepository.findByName("ROLE_USER").orElse(null);
            // получаем ID для пользователя
            twoUser = userRepository.save(new User(null, "Mark", "12345678", "mark@mail.com", true, new HashSet<>()));
            threeUser = userRepository.save(new User(null, "Sergey", "12345678", "sergey@mail.com", true, new HashSet<>()));
            // теперь назначаем роли
            if (role != null) {
                twoUser.addRole(role);
                userRepository.save(twoUser);
                threeUser.addRole(role);
                userRepository.save(threeUser);
            }
        }


        @Test
        @DisplayName("Выборка всех пользователей")
        void getAllUsers() {
            var page = userService.getAllUsers(new UserFilter(null, null, null, null), PageRequest.of(0, 10));

            assertThat(page).isNotNull();
            assertThat(page.getContent()).hasSize(3);

            List<UUID> uuids = page.getContent().stream().map(UserResponse::getUserId).toList();
            List<String> names = page.getContent().stream().map(UserResponse::getUserName).toList();

            assertThat(uuids).containsExactlyInAnyOrder(defaultUser.getId(), twoUser.getId(), threeUser.getId());
            assertThat(names).containsExactlyInAnyOrder("Ivan", "Mark", "Sergey");
        }

        @Test
        @DisplayName("Выборка по роли")
        void getFilteredRole() {
            var page = userService.getAllUsers(new UserFilter(null, null, null, "ADMIN"), PageRequest.of(0, 10));

            assertThat(page).isNotNull();
            assertThat(page.getContent()).hasSize(1);

            assertThat(page.getContent().get(0).getUserId()).isEqualTo(defaultUser.getId());
            assertThat(page.getContent().get(0).getUserName()).isEqualTo("Ivan");
        }

        @Test
        @DisplayName("Выборка по имени")
        void getFilteredName() {
            var page = userService.getAllUsers(new UserFilter("Iv", null, null, null), PageRequest.of(0, 10));

            assertThat(page).isNotNull();
            assertThat(page.getContent()).hasSize(1);

            assertThat(page.getContent().get(0).getUserId()).isEqualTo(defaultUser.getId());
            assertThat(page.getContent().get(0).getUserName()).isEqualTo("Ivan");
        }

        @Test
        @DisplayName("Пагинация")
        void getPage() {
            Pageable pageable = PageRequest.of(0, 2);
            UserFilter userFilter = new UserFilter();

            // делаем выборку первой страницы
            var page = userService.getAllUsers(userFilter, pageable);

            assertThat(page).isNotNull();
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getTotalElements()).isEqualTo(3);

            // делаем выборку второй страницы
            pageable = PageRequest.of(1, 2);
            page = userService.getAllUsers(userFilter, pageable);

            assertThat(page).isNotNull();
            assertThat(page.getContent()).hasSize(1);
        }
    }

}


//User user = new User(null, "Ivan", "12345678", "test@mail.com", true, new HashSet<>());
