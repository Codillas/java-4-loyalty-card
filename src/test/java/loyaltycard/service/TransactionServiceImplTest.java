package loyaltycard.service;

import loyaltycard.exception.*;
import loyaltycard.mapper.TransactionMapper;
import loyaltycard.repository.CardRepository;
import loyaltycard.repository.TransactionRepository;
import loyaltycard.repository.entity.CardEntity;
import loyaltycard.repository.entity.DirectionEntity;
import loyaltycard.repository.entity.StatusEntity;
import loyaltycard.repository.entity.TransactionEntity;
import loyaltycard.repository.entity.TransactionStatusEntity;
import loyaltycard.service.model.Direction;
import loyaltycard.service.model.Transaction;
import loyaltycard.service.model.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-тести для TransactionServiceImpl з Mockito.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    // ---------------------------------------------------------------------------
    // Допоміжні методи
    // ---------------------------------------------------------------------------

    private CardEntity buildCardEntity(UUID cardId, int balance, StatusEntity status) {
        CardEntity entity = new CardEntity();
        entity.setId(cardId);
        entity.setCustomerId(UUID.randomUUID());
        entity.setBalance(balance);
        entity.setStatusEntity(status);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    private TransactionEntity buildTransactionEntity(UUID id, UUID cardId, DirectionEntity direction,
                                                     int amount, TransactionStatusEntity status) {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(id);
        entity.setAdminId(UUID.randomUUID());
        entity.setCardId(cardId);
        entity.setDirection(direction);
        entity.setAmount(amount);
        entity.setStatus(status);
        entity.setNote("test note");
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    private Transaction buildTransaction(UUID id, UUID cardId, Direction direction,
                                         int amount, TransactionStatus status) {
        Transaction tx = new Transaction();
        tx.setId(id);
        tx.setAdminId(UUID.randomUUID());
        tx.setCardId(cardId);
        tx.setDirection(direction);
        tx.setAmount(amount);
        tx.setStatus(status);
        tx.setNote("test note");
        tx.setCreatedAt(Instant.now());
        tx.setUpdatedAt(Instant.now());
        return tx;
    }

    // ===========================================================================
    // createTransaction()
    // ===========================================================================

    @Test
    @DisplayName("createTransaction: зараховує суму (IN) і зберігає транзакцію")
    void shouldCreateInTransactionSuccessfully() {

        // given
        UUID cardId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        CardEntity cardEntity = buildCardEntity(cardId, 100, StatusEntity.ACTIVE);
        TransactionEntity savedTxEntity = buildTransactionEntity(txId, cardId, DirectionEntity.IN, 50, TransactionStatusEntity.SUCCESS);
        Transaction expectedTx = buildTransaction(txId, cardId, Direction.IN, 50, TransactionStatus.SUCCESS);

        Transaction inputTx = new Transaction();
        inputTx.setDirection(Direction.IN);
        inputTx.setAmount(50);
        inputTx.setNote("test note");

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(cardEntity));
        when(transactionMapper.toEntity(inputTx)).thenReturn(savedTxEntity);
        when(transactionRepository.save(savedTxEntity)).thenReturn(savedTxEntity);
        when(transactionMapper.toDomain(savedTxEntity)).thenReturn(expectedTx);

        // when
        Transaction result = transactionService.createTransaction(cardId, inputTx, adminId);

        // then
        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        // Баланс повинен збільшитись на 50 (100 + 50 = 150)
        assertEquals(150, cardEntity.getBalance());
        verify(cardRepository).save(cardEntity);
        verify(transactionRepository).save(savedTxEntity);
    }

    @Test
    @DisplayName("createTransaction: списує суму (OUT) при достатньому балансі")
    void shouldCreateOutTransactionSuccessfully() {

        // given
        UUID cardId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        CardEntity cardEntity = buildCardEntity(cardId, 200, StatusEntity.ACTIVE);
        TransactionEntity savedTxEntity = buildTransactionEntity(txId, cardId, DirectionEntity.OUT, 50, TransactionStatusEntity.SUCCESS);
        Transaction expectedTx = buildTransaction(txId, cardId, Direction.OUT, 50, TransactionStatus.SUCCESS);

        Transaction inputTx = new Transaction();
        inputTx.setDirection(Direction.OUT);
        inputTx.setAmount(50);
        inputTx.setNote("test note");

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(cardEntity));
        when(transactionMapper.toEntity(inputTx)).thenReturn(savedTxEntity);
        when(transactionRepository.save(savedTxEntity)).thenReturn(savedTxEntity);
        when(transactionMapper.toDomain(savedTxEntity)).thenReturn(expectedTx);

        // when
        Transaction result = transactionService.createTransaction(cardId, inputTx, adminId);

        // then
        assertNotNull(result);
        // Баланс повинен зменшитись на 50 (200 - 50 = 150)
        assertEquals(150, cardEntity.getBalance());
    }

    @Test
    @DisplayName("createTransaction: кидає CardNotFoundException, якщо картки не існує")
    void shouldThrowCardNotFoundExceptionWhenCardDoesNotExist() {

        UUID cardId = UUID.randomUUID();
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class,
                () -> transactionService.createTransaction(cardId, new Transaction(), UUID.randomUUID()));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTransaction: кидає CardIsBlockedException, якщо картку заблоковано")
    void shouldThrowCardIsBlockedExceptionWhenCardIsBlocked() {

        UUID cardId = UUID.randomUUID();
        CardEntity blockedCard = buildCardEntity(cardId, 100, StatusEntity.BLOCKED);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(blockedCard));

        Transaction inputTx = new Transaction();
        inputTx.setDirection(Direction.IN);
        inputTx.setAmount(50);

        assertThrows(CardIsBlockedException.class,
                () -> transactionService.createTransaction(cardId, inputTx, UUID.randomUUID()));
    }

    @Test
    @DisplayName("createTransaction: кидає InsufficientBalanceException при нестачі коштів (OUT)")
    void shouldThrowInsufficientBalanceException() {

        UUID cardId = UUID.randomUUID();
        // Баланс = 30, але списуємо 100
        CardEntity cardEntity = buildCardEntity(cardId, 30, StatusEntity.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(cardEntity));

        Transaction inputTx = new Transaction();
        inputTx.setDirection(Direction.OUT);
        inputTx.setAmount(100);

        assertThrows(InsufficientBalanceException.class,
                () -> transactionService.createTransaction(cardId, inputTx, UUID.randomUUID()));
    }

    // ===========================================================================
    // getTransactions()
    // ===========================================================================

    @Test
    @DisplayName("getTransactions: повертає список транзакцій для картки")
    void shouldReturnTransactionsForCard() {

        UUID cardId = UUID.randomUUID();
        UUID txId1 = UUID.randomUUID();
        UUID txId2 = UUID.randomUUID();

        TransactionEntity te1 = buildTransactionEntity(txId1, cardId, DirectionEntity.IN, 100, TransactionStatusEntity.SUCCESS);
        TransactionEntity te2 = buildTransactionEntity(txId2, cardId, DirectionEntity.OUT, 50, TransactionStatusEntity.SUCCESS);
        Transaction t1 = buildTransaction(txId1, cardId, Direction.IN, 100, TransactionStatus.SUCCESS);
        Transaction t2 = buildTransaction(txId2, cardId, Direction.OUT, 50, TransactionStatus.SUCCESS);

        when(cardRepository.existsById(cardId)).thenReturn(true);
        when(transactionRepository.findAllByCardId(cardId)).thenReturn(List.of(te1, te2));
        when(transactionMapper.toDomain(te1)).thenReturn(t1);
        when(transactionMapper.toDomain(te2)).thenReturn(t2);

        List<Transaction> result = transactionService.getTransactions(cardId);

        assertEquals(2, result.size());
        verify(transactionRepository).findAllByCardId(cardId);
    }

    @Test
    @DisplayName("getTransactions: кидає CardNotFoundException, якщо картки не існує")
    void shouldThrowWhenGettingTransactionsForNonExistentCard() {

        UUID cardId = UUID.randomUUID();
        when(cardRepository.existsById(cardId)).thenReturn(false);

        assertThrows(CardNotFoundException.class, () -> transactionService.getTransactions(cardId));
    }

    // ===========================================================================
    // getTransactionById()
    // ===========================================================================

    @Test
    @DisplayName("getTransactionById: повертає транзакцію, якщо вона існує")
    void shouldReturnTransactionById() {

        UUID cardId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();
        TransactionEntity entity = buildTransactionEntity(txId, cardId, DirectionEntity.IN, 100, TransactionStatusEntity.SUCCESS);
        Transaction expectedTx = buildTransaction(txId, cardId, Direction.IN, 100, TransactionStatus.SUCCESS);

        when(cardRepository.existsById(cardId)).thenReturn(true);
        when(transactionRepository.findByIdAndCardId(txId, cardId)).thenReturn(Optional.of(entity));
        when(transactionMapper.toDomain(entity)).thenReturn(expectedTx);

        Transaction result = transactionService.getTransactionById(cardId, txId);

        assertNotNull(result);
        assertEquals(txId, result.getId());
    }

    @Test
    @DisplayName("getTransactionById: кидає TransactionNotFoundException, якщо транзакція не знайдена")
    void shouldThrowTransactionNotFoundExceptionWhenTransactionDoesNotExist() {

        UUID cardId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        when(cardRepository.existsById(cardId)).thenReturn(true);
        when(transactionRepository.findByIdAndCardId(txId, cardId)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getTransactionById(cardId, txId));
    }

    // ===========================================================================
    // cancelTransaction()
    // ===========================================================================

    @Test
    @DisplayName("cancelTransaction: скасовує IN-транзакцію і зменшує баланс")
    void shouldCancelInTransactionAndRevertBalance() {

        UUID cardId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        CardEntity cardEntity = buildCardEntity(cardId, 150, StatusEntity.ACTIVE);
        TransactionEntity txEntity = buildTransactionEntity(txId, cardId, DirectionEntity.IN, 50, TransactionStatusEntity.SUCCESS);
        TransactionEntity savedTxEntity = buildTransactionEntity(txId, cardId, DirectionEntity.IN, 50, TransactionStatusEntity.CANCELED);
        Transaction expectedTx = buildTransaction(txId, cardId, Direction.IN, 50, TransactionStatus.CANCELED);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(cardEntity));
        when(transactionRepository.findByIdAndCardId(txId, cardId)).thenReturn(Optional.of(txEntity));
        when(cardRepository.save(cardEntity)).thenReturn(cardEntity);
        when(transactionRepository.save(txEntity)).thenReturn(savedTxEntity);
        when(transactionMapper.toDomain(savedTxEntity)).thenReturn(expectedTx);

        Transaction result = transactionService.cancelTransaction(cardId, txId);

        assertNotNull(result);
        assertEquals(TransactionStatus.CANCELED, result.getStatus());
        // Баланс зменшився на 50 (150 - 50 = 100), бо скасовуємо зарахування
        assertEquals(100, cardEntity.getBalance());
        assertEquals(TransactionStatusEntity.CANCELED, txEntity.getStatus());
    }

    @Test
    @DisplayName("cancelTransaction: кидає TransactionAlreadyCanceledException, якщо транзакцію вже скасовано")
    void shouldThrowWhenTransactionAlreadyCanceled() {

        UUID cardId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        CardEntity cardEntity = buildCardEntity(cardId, 100, StatusEntity.ACTIVE);
        TransactionEntity alreadyCanceled = buildTransactionEntity(txId, cardId, DirectionEntity.IN, 50, TransactionStatusEntity.CANCELED);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(cardEntity));
        when(transactionRepository.findByIdAndCardId(txId, cardId)).thenReturn(Optional.of(alreadyCanceled));

        assertThrows(TransactionAlreadyCanceledException.class,
                () -> transactionService.cancelTransaction(cardId, txId));
    }
}
