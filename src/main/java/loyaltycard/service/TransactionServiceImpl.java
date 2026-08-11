package loyaltycard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import loyaltycard.exception.CardIsBlockedException;
import loyaltycard.exception.CardNotFoundException;
import loyaltycard.exception.InsufficientBalanceException;
import loyaltycard.exception.TransactionAlreadyCanceledException;
import loyaltycard.exception.TransactionNotFoundException;
import loyaltycard.mapper.TransactionMapper;
import loyaltycard.repository.CardRepository;
import loyaltycard.repository.TransactionRepository;
import loyaltycard.repository.entity.CardEntity;
import loyaltycard.repository.entity.TransactionEntity;
import loyaltycard.repository.entity.TransactionStatusEntity;
import loyaltycard.service.model.Direction;
import loyaltycard.service.model.Transaction;
import loyaltycard.service.model.TransactionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public Transaction createTransaction(UUID cardId, Transaction transaction, UUID adminId) {

        log.info("Creating transaction for card with id {}", cardId);

        CardEntity cardEntity = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (cardEntity.getStatusEntity().name().equals("BLOCKED")) {
            throw new CardIsBlockedException(cardId);
        }

        // Apply balance change
        if (transaction.getDirection() == Direction.IN) {
            cardEntity.setBalance(cardEntity.getBalance() + transaction.getAmount());
        } else {
            if (cardEntity.getBalance() < transaction.getAmount()) {
                throw new InsufficientBalanceException(cardId);
            }
            cardEntity.setBalance(cardEntity.getBalance() - transaction.getAmount());
        }
        cardEntity.setUpdatedAt(Instant.now());
        cardRepository.save(cardEntity);

        // Build and persist transaction
        transaction.setCardId(cardId);
        transaction.setAdminId(adminId);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreatedAt(Instant.now());
        transaction.setUpdatedAt(Instant.now());

        TransactionEntity transactionEntity = transactionMapper.toEntity(transaction);
        TransactionEntity saved = transactionRepository.save(transactionEntity);

        log.info("Successfully created transaction for card with id {}", cardId);

        return transactionMapper.toDomain(saved);
    }

    @Override
    public List<Transaction> getTransactions(UUID cardId) {

        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException(cardId);
        }

        List<TransactionEntity> entities = transactionRepository.findAllByCardId(cardId);

        return entities.stream()
                .map(transactionMapper::toDomain)
                .toList();
    }

    @Override
    public Transaction getTransactionById(UUID cardId, UUID transactionId) {

        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException(cardId);
        }

        TransactionEntity transactionEntity = transactionRepository.findByIdAndCardId(transactionId, cardId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        return transactionMapper.toDomain(transactionEntity);
    }

    @Override
    @Transactional
    public Transaction cancelTransaction(UUID cardId, UUID transactionId) {

        log.info("Canceling transaction with id {}", transactionId);

        CardEntity cardEntity = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        TransactionEntity transactionEntity = transactionRepository.findByIdAndCardId(transactionId, cardId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        if (transactionEntity.getStatus() == TransactionStatusEntity.CANCELED) {
            throw new TransactionAlreadyCanceledException(transactionId);
        }

        // Revert balance change
        if (transactionEntity.getDirection().name().equals("IN")) {
            cardEntity.setBalance(cardEntity.getBalance() - transactionEntity.getAmount());
        } else {
            cardEntity.setBalance(cardEntity.getBalance() + transactionEntity.getAmount());
        }
        cardEntity.setUpdatedAt(Instant.now());
        cardRepository.save(cardEntity);

        // Mark transaction as canceled
        transactionEntity.setStatus(TransactionStatusEntity.CANCELED);
        transactionEntity.setUpdatedAt(Instant.now());
        TransactionEntity saved = transactionRepository.save(transactionEntity);

        log.info("Successfully canceled transaction with id {}", transactionId);

        return transactionMapper.toDomain(saved);
    }
}
