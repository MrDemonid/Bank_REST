package com.example.bankcards.service;

import com.example.bankcards.dto.PageDTO;
import com.example.bankcards.dto.SignUpRequest;
import com.example.bankcards.dto.UpdateUserRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.auth.User;
import com.example.bankcards.exception.BankCardErrorCodes;
import com.example.bankcards.exception.BankCardException;
import com.example.bankcards.exception.UserOperationException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CustomPasswordEncoder;
import com.example.bankcards.dto.filters.UserFilter;
import com.example.bankcards.dto.filters.UserSpecification;
import com.example.bankcards.service.mappers.RoleMapper;
import com.example.bankcards.service.mappers.UserMapper;
import com.example.bankcards.service.validators.UserValidator;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


/**
 * Реализация интерфейса сервисного слоя для работы с пользователями.
 * В случае ошибок выбрасываются исключения BankCardException, перехватывающиеся
 * глобальным контроллером исключений, который и отправит отчет об ошибке вызывающей стороне.
 */
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomPasswordEncoder customPasswordEncoder;
    private final UserMapper userMapper;
    private final UserValidator userValidator;
    private final RoleMapper roleMapper;


    /**
     * Возвращает список пользователей.
     * @param filter   Фильтр отбора пользователей.
     * @param pageable Пагинация (страница и её размер)
     * @return         Страничная выборка пользователей, в соответствии с заданными критериями.
     */
    @Override
    public PageDTO<UserResponse> getAllUsers(UserFilter filter, Pageable pageable) {
        try {
            Page<User> users = userRepository.findAll(UserSpecification.filterBy(filter), pageable);
            return userMapper.toPageUser(users);
        } catch (Exception ignored) {
        }
        return PageDTO.empty();
    }

    @Override
    public Boolean existsUser(UUID userId) {
        try {
            return userRepository.existsById(userId);
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * Создание нового пользователя.
     * @param userRequest Структура с данными пользователя.
     * @return Данные о пользователе, в случае успеха.
     */
    @Override
    @Transactional
    public UserResponse createUser(SignUpRequest userRequest) {
        try {
            if (userRepository.findByUsername(userRequest.getUserName()).isPresent()) {
                throw new UserOperationException(BankCardErrorCodes.USER_ALREADY_EXISTS, "user already exists");
            }
            // проверяем корректность данных
            userValidator.validateUserRequest(userRequest);
            // создаем сущность и сохраняем в БД, данные уже проверены.
            User user = userMapper.toSignUpUser(userRequest, roleMapper.toRoles(userRequest.getRoles()));

            return userMapper.toResponse(userRepository.save(user));

        } catch (Exception e) {
            if (e instanceof BankCardException) {
                throw e;
            }
            throw new UserOperationException(BankCardErrorCodes.UNKNOWN_ERROR_CODE, e.getMessage());
        }
    }

    /**
     * Обновление данных пользователя.
     * @param updateUserRequest Структура с данными пользователя.
     * @return Данные о пользователе, в случае успеха.
     */
    @Override
    @Transactional
    public UserResponse updateUser(UpdateUserRequest updateUserRequest) {
        try {
            User user = userRepository.findById(updateUserRequest.getId()).orElse(null);
            if (user == null) {
                throw new UserOperationException(BankCardErrorCodes.USER_NOT_FOUND, "user does not exist");
            }
            // проверяем корректность данных
            userValidator.validateUserRequest(updateUserRequest);

            // обновляем поля
            user.setEmail(updateUserRequest.getEmail());
            user.setUsername(updateUserRequest.getUserName());
            user.setPassword(customPasswordEncoder.encode(updateUserRequest.getPassword()));
            user.setEnabled(updateUserRequest.isEnabled());
            user.setRoles(roleMapper.toRoles(updateUserRequest.getRoles()));

            user = userRepository.save(user);
            return userMapper.toResponse(user);

        } catch (Exception e) {
            if (e instanceof BankCardException) {
                throw e;
            }
            throw new UserOperationException(BankCardErrorCodes.UNKNOWN_ERROR_CODE, e.getMessage());
        }
    }

    /**
     * Удаление пользователя.
     * @param userId Идентификатор пользователя.
     */
    @Override
    public void deleteUser(UUID userId) {
        try {
            if (!userRepository.existsById(userId)) {
                throw new UserOperationException(BankCardErrorCodes.USER_NOT_FOUND, "user does not exist");
            }
            userRepository.deleteById(userId);
        } catch (Exception e) {
            if (e instanceof BankCardException) {
                throw e;
            }
            throw new UserOperationException(BankCardErrorCodes.UNKNOWN_ERROR_CODE, e.getMessage());
        }
    }


}
