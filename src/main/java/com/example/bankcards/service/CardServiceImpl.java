package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.PageDTO;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.BlockedCards;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardAction;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.BankCardErrorCodes;
import com.example.bankcards.exception.BankCardException;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.repository.BlockedCardsRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.dto.filters.CardFilter;
import com.example.bankcards.dto.filters.CardSpecification;
import com.example.bankcards.service.mappers.CardMapper;
import com.example.bankcards.util.CardNumberHasher;
import com.example.bankcards.util.CardUtil;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Реализация интерфейса сервисного слоя для работы с картами.
 * В случае ошибок выбрасываются исключения BankCardException, перехватывающиеся
 * глобальным контроллером исключений, который и отправит отчет об ошибке вызывающей стороне.
 */
@Service
@AllArgsConstructor
@Log4j2
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final BlockedCardsRepository blockedCardsRepository;
    private final CardMapper cardMapper;
    private final UserService userService;


    /**
     * Создание новой карты.
     * @param request Запрос с необходимыми данными для новой карты.
     * @return Информация по карте.
     */
    @Override
    public CardResponse createCard(CardCreateRequest request) {
        if (!CardUtil.isCardNumberValid(request.getCardNumber())) {
            throw new CardOperationException(BankCardErrorCodes.CARD_INVALID_NUMBER, "card number is invalid");
        }
        if (!CardUtil.isCardExpiryValid(request.getExpiryDate())) {
            throw new CardOperationException(BankCardErrorCodes.CARD_INVALID_EXPIRATION, "card expiry is invalid");
        }
        if (!userService.existsUser(request.getUserId())) {
            throw new CardOperationException(BankCardErrorCodes.USER_NOT_FOUND, "user does not exist");
        }
        String cardNumber = CardUtil.normalizeCardNumber(request.getCardNumber());
        String hmac = CardNumberHasher.hmacSha256(cardNumber);
        if (cardRepository.existsByCardNumberHmac(hmac)) {
            throw new CardOperationException(BankCardErrorCodes.CARD_ALREADY_EXISTS, "card number already exists");
        }
        BigDecimal balance = request.getBalance() == null ? BigDecimal.ZERO : request.getBalance();

        Card card = new Card(
                null,
                cardNumber,
                hmac,
                request.getExpiryDate(),
                CardStatus.ACTIVE,
                balance,
                request.getUserId()
        );
        try {
            Card res = cardRepository.save(card);
            log.info("Create new card: {}", res);  // номер будет в скрытом виде.
            return cardMapper.toCardResponse(res);
        } catch (Exception e) {
            if (e instanceof BankCardException) {
                throw e;
            }
            throw new CardOperationException(BankCardErrorCodes.UNKNOWN_ERROR_CODE, e.getMessage());
        }
    }

    /**
     * Активация пользовательской карты.
     * @param userId Идентификатор пользователя.
     * @param id     Идентификатор карты.
     * @return Информация по карте.
     */
    @Override
    public CardResponse activateCard(UUID userId, Long id) {
        try {
            Card card = cardRepository.findById(id).orElse(null);
            if (card == null) {
                throw new CardOperationException(BankCardErrorCodes.CARD_NOT_FOUND, "card does not exist");
            }
            if (!card.getUserId().equals(userId)) {
                throw new CardOperationException(BankCardErrorCodes.CARD_INVALID_OWNER, "incorrect card owner");
            }
            card.setStatus(CardStatus.ACTIVE);
            Card res = cardRepository.save(card);
            return cardMapper.toCardResponse(res);
        } catch (Exception e) {
            if (e instanceof BankCardException) {
                throw e;
            }
            throw new CardOperationException(BankCardErrorCodes.UNKNOWN_ERROR_CODE, e.getMessage());
        }
    }

    /**
     * Блокирование карты пользователя.
     * @param userId Идентификатор пользователя.
     * @param id     Идентификатор карты.
     * @return Информация по карте.
     */
    @Override
    public CardResponse blockedCard(UUID userId, Long id) {
        try {
            Card card = cardRepository.findById(id).orElse(null);
            if (card == null) {
                throw new CardOperationException(BankCardErrorCodes.CARD_NOT_FOUND, "card does not exist");
            }
            if (!card.getUserId().equals(userId)) {
                throw new CardOperationException(BankCardErrorCodes.CARD_INVALID_OWNER, "incorrect card owner");
            }
            card.setStatus(CardStatus.BLOCKED);
            Card res = cardRepository.save(card);
            return cardMapper.toCardResponse(res);
        } catch (Exception e) {
            if (e instanceof BankCardException) {
                throw e;
            }
            throw new CardOperationException(BankCardErrorCodes.UNKNOWN_ERROR_CODE, e.getMessage());
        }
    }

    /**
     * Удаление карты пользователя.
     * @param userId Идентификатор пользователя.
     * @param id     Идентификатор карты.
     */
    @Override
    public void deleteCard(UUID userId, Long id) {
        try {
            Card card = cardRepository.findById(id).orElse(null);
            if (card == null) {
                throw new CardOperationException(BankCardErrorCodes.CARD_NOT_FOUND, "card does not exist");
            }
            if (!card.getUserId().equals(userId)) {
                throw new CardOperationException(BankCardErrorCodes.CARD_INVALID_OWNER, "incorrect card owner");
            }
            cardRepository.delete(card);
        } catch (Exception e) {
            if (e instanceof BankCardException) {
                throw e;
            }
            throw new CardOperationException(BankCardErrorCodes.UNKNOWN_ERROR_CODE, e.getMessage());
        }

    }

    /**
     * Выборка всех карт из БД с использованием фильтра и пагинации.
     * @param filter   Фильтр.
     * @param pageable Информация о выбираемой странице.
     * @return Страница со списком карт.
     */
    @Override
    public PageDTO<CardResponse> getAllCards(CardFilter filter, Pageable pageable) {
        try {
            Page<Card> cards = cardRepository.findAll(CardSpecification.filterBy(filter), pageable);
            return cardMapper.toPageCard(cards);
        } catch (Exception e) {
            throw new CardOperationException(BankCardErrorCodes.UNKNOWN_ERROR_CODE, e.getMessage());
        }
    }

    /**
     * Выборка карт пользователя из БД с использованием фильтра и пагинации.
     * @param userId   Идентификатор пользователя.
     * @param filter   Фильтр.
     * @param pageable Информация о выбираемой странице.
     * @return Страница со списком карт.
     */
    @Override
    public PageDTO<CardResponse> getUserCards(UUID userId, CardFilter filter, Pageable pageable) {
        try {
            if (userService.existsUser(userId)) {
                filter.setUserId(userId);
                Specification<Card> spec = CardSpecification.filterBy(filter);
                Page<Card> cards = cardRepository.findAll(spec, pageable);
                return cardMapper.toPageCard(cards);
            }
            throw new CardOperationException(BankCardErrorCodes.USER_NOT_FOUND, "user does not exist");
        } catch (Exception e) {
            if (e instanceof BankCardException) {
                throw e;
            }
            throw new CardOperationException(BankCardErrorCodes.UNKNOWN_ERROR_CODE, e.getMessage());
        }
    }

    /**
     * Запрос на блокировку карты.
     * @param userId Идентификатор пользователя.
     * @param cardId Идентификатор карты.
     */
    @Override
    public void requestToBlockingCard(UUID userId, Long cardId) {
        try {
            if (!userService.existsUser(userId)) {
                throw new CardOperationException(BankCardErrorCodes.USER_NOT_FOUND, "user does not exist");
            }
            if (blockedCardsRepository.existsBlockedCardsByCardId(cardId)) {
                throw new CardOperationException(BankCardErrorCodes.CARD_ALREADY_BLOCKED, "card already blocked");
            }
            Card card = cardRepository.findById(cardId).orElse(null);
            if (card == null || !card.getUserId().equals(userId)) {
                throw new CardOperationException(BankCardErrorCodes.CARD_NOT_FOUND, "card does not exist");
            }
            BlockedCards blockedCards = new BlockedCards(
                    null,
                    cardId,
                    CardAction.BLOCK,
                    LocalDateTime.now()
            );
            blockedCardsRepository.save(blockedCards);
        } catch (Exception e) {
            if (e instanceof BankCardException) {
                throw e;
            }
            throw new CardOperationException(BankCardErrorCodes.UNKNOWN_ERROR_CODE, e.getMessage());
        }
    }

    /**
     * Перевод средств между картами пользователя.
     * @param request Данные о переводе (владелец, откуда, куда и сколько).
     */
    @Override
    @Transactional
    public void transferAmount(TransferRequest request) {
        try {
            if (!userService.existsUser(request.getUserId())) {
                throw new CardOperationException(BankCardErrorCodes.USER_NOT_FOUND, "user does not exist");
            }
            Card fromCard = cardRepository.findById(request.getFromCardId()).orElse(null);
            Card toCard = cardRepository.findById(request.getToCardId()).orElse(null);
            if (fromCard == null || toCard == null) {
                throw new CardOperationException(BankCardErrorCodes.CARD_NOT_FOUND, "card not found");
            }
            if (fromCard.getId().equals(toCard.getId())) {
                throw new CardOperationException(BankCardErrorCodes.CARD_CANNOT_BE_SAME, "cards cannot be the same");
            }
            if (!fromCard.getUserId().equals(request.getUserId()) || !toCard.getUserId().equals(request.getUserId())) {
                throw new CardOperationException(BankCardErrorCodes.CARD_INVALID_OWNER, "user does not own cards");
            }
            if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
                throw new CardOperationException(BankCardErrorCodes.CARD_BAD_STATUS, "card status is not active");
            }
            if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
                throw new CardOperationException(BankCardErrorCodes.CARD_NOT_ENOUGHT_FUNDS, "from card does not have enough balance");
            }
            // списание и зачисление
            fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
            toCard.setBalance(toCard.getBalance().add(request.getAmount()));
            // сохраняем
            cardRepository.save(fromCard);
            cardRepository.save(toCard);

            log.info("Осуществлен перевод с карты {} на карту {}", CardUtil.getMaskedNumber(fromCard.getCardNumber()), CardUtil.getMaskedNumber(toCard.getCardNumber()));
            // TODO: добавить логирование операции!!!

        } catch (Exception e) {
            if (e instanceof BankCardException) {
                throw e;
            }
            throw new CardOperationException(BankCardErrorCodes.UNKNOWN_ERROR_CODE, e.getMessage());
        }
    }

    /**
     * Возвращает баланс карты.
     * @param id     Идентификатор пользователя.
     * @param cardId Идентификатор его карты.
     * @return Текущий баланс карты.
     */
    @Override
    public BigDecimal getBalance(UUID id, Long cardId) {
        try {
            if (!userService.existsUser(id)) {
                throw new CardOperationException(BankCardErrorCodes.USER_NOT_FOUND, "user does not exist");
            }
            Card card = cardRepository.findById(cardId).orElse(null);
            if (card == null) {
                throw new CardOperationException(BankCardErrorCodes.CARD_NOT_FOUND, "card does not exist");
            }
            if (!card.getUserId().equals(id)) {
                throw new CardOperationException(BankCardErrorCodes.CARD_INVALID_OWNER, "card does not own cards");
            }
            if (card.getStatus() != CardStatus.ACTIVE) {
                throw new CardOperationException(BankCardErrorCodes.CARD_BAD_STATUS, "card status is not active");
            }
            return card.getBalance();

        } catch (Exception e) {
            if (e instanceof BankCardException) {
                throw e;
            }
            throw new CardOperationException(BankCardErrorCodes.UNKNOWN_ERROR_CODE, e.getMessage());
        }
    }


}
