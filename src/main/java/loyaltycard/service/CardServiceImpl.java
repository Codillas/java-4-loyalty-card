package loyaltycard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import loyaltycard.exception.CardAlreadyExistsException;
import loyaltycard.exception.CardNotFoundException;
import loyaltycard.exception.CustomerNotFoundException;
import loyaltycard.mapper.CardMapper;
import loyaltycard.repository.CardRepository;
import loyaltycard.repository.CustomerRepository;
import loyaltycard.repository.entity.CardEntity;
import loyaltycard.repository.entity.StatusEntity;
import loyaltycard.service.model.Card;
import loyaltycard.service.model.Status;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CustomerRepository customerRepository;
    private final CardMapper cardMapper;

    @Override
    public Card createCard(UUID customerId) {

        log.info("Creating a card for customer with id {}", customerId);

        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }

        if (cardRepository.findByCustomerId(customerId).isPresent()) {
            throw new CardAlreadyExistsException(customerId);
        }

        Card card = new Card();
        card.setCustomerId(customerId);
        card.setBalance(0);
        card.setStatus(Status.ACTIVE);
        card.setCreatedAt(Instant.now());
        card.setUpdatedAt(Instant.now());

        CardEntity cardEntity = cardMapper.toEntity(card);
        CardEntity savedCard = cardRepository.save(cardEntity);

        log.info("Successfully created a card for customer with id {}", customerId);

        return cardMapper.toDomain(savedCard);
    }

    @Override
    public Card getCardById(UUID cardId) {

        Optional<CardEntity> optionalCard = cardRepository.findById(cardId);

        if (optionalCard.isEmpty()) {
            throw new CardNotFoundException(cardId);
        }

        return cardMapper.toDomain(optionalCard.get());
    }

    @Override
    public Card activateCard(UUID cardId) {

        Optional<CardEntity> optionalCard = cardRepository.findById(cardId);

        if (optionalCard.isEmpty()) {
            throw new CardNotFoundException(cardId);
        }

        CardEntity cardEntity = optionalCard.get();
        cardEntity.setStatusEntity(StatusEntity.ACTIVE);
        cardEntity.setUpdatedAt(Instant.now());
        CardEntity savedCard = cardRepository.save(cardEntity);

        return cardMapper.toDomain(savedCard);
    }

    @Override
    public Card blockCard(UUID cardId) {

        Optional<CardEntity> optionalCard = cardRepository.findById(cardId);

        if (optionalCard.isEmpty()) {
            throw new CardNotFoundException(cardId);
        }

        CardEntity cardEntity = optionalCard.get();
        cardEntity.setStatusEntity(StatusEntity.BLOCKED);
        cardEntity.setUpdatedAt(Instant.now());
        CardEntity savedCard = cardRepository.save(cardEntity);

        return cardMapper.toDomain(savedCard);
    }

    @Override
    public boolean isCardOwner(UUID cardId, String principalId) {
        Optional<CardEntity> cardOptional = cardRepository.findById(cardId);
        if (cardOptional.isEmpty()) {
            return false;
        }
        return cardOptional.get().getCustomerId().toString().equals(principalId);
    }
}
