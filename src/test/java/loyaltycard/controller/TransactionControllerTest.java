package loyaltycard.controller;

import loyaltycard.controller.dto.CreateTransactionRequestDto;
import loyaltycard.controller.dto.DirectionDto;
import loyaltycard.controller.dto.TransactionDto;
import loyaltycard.controller.dto.TransactionStatusDto;
import loyaltycard.mapper.TransactionMapper;
import loyaltycard.service.TransactionService;
import loyaltycard.service.model.Direction;
import loyaltycard.service.model.Transaction;
import loyaltycard.service.model.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionControllerTest {

    // --- Stub implementation ---

    private static class StubTransactionService implements TransactionService {

        final UUID fixedTransactionId = UUID.fromString("eeeeeeee-0000-0000-0000-000000000001");
        final UUID fixedCardId        = UUID.fromString("dddddddd-0000-0000-0000-000000000001");
        final UUID fixedAdminId       = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");

        private Transaction buildTransaction(Direction direction, int amount, TransactionStatus status) {
            Transaction tx = new Transaction();
            tx.setId(fixedTransactionId);
            tx.setAdminId(fixedAdminId);
            tx.setCardId(fixedCardId);
            tx.setDirection(direction);
            tx.setAmount(amount);
            tx.setStatus(status);
            tx.setNote("Test note");
            tx.setCreatedAt(Instant.now());
            tx.setUpdatedAt(Instant.now());
            return tx;
        }

        @Override
        public Transaction createTransaction(UUID cardId, Transaction transaction, UUID adminId) {
            return buildTransaction(transaction.getDirection(), transaction.getAmount(), TransactionStatus.SUCCESS);
        }

        @Override
        public List<Transaction> getTransactions(UUID cardId) {
            return List.of(
                    buildTransaction(Direction.IN, 100, TransactionStatus.SUCCESS),
                    buildTransaction(Direction.OUT, 50, TransactionStatus.SUCCESS)
            );
        }

        @Override
        public Transaction getTransactionById(UUID cardId, UUID transactionId) {
            return buildTransaction(Direction.IN, 100, TransactionStatus.SUCCESS);
        }

        @Override
        public Transaction cancelTransaction(UUID cardId, UUID transactionId) {
            return buildTransaction(Direction.IN, 100, TransactionStatus.CANCELED);
        }
    }

    private TransactionController transactionController;
    private StubTransactionService stubTransactionService;

    private UserDetails adminPrincipal(UUID adminId) {
        return User.withUsername(adminId.toString())
                .password("n/a")
                .authorities("ADMIN")
                .build();
    }

    @BeforeEach
    void setUp() {
        stubTransactionService = new StubTransactionService();
        TransactionMapper transactionMapper = new TransactionMapper();
        transactionController = new TransactionController(stubTransactionService, transactionMapper);
    }

    // -----------------------------------------------------------------------
    // POST /cards/{cardId}/transactions
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("createTransaction: returns 201 CREATED with SUCCESS status")
    void shouldReturnCreatedWhenTransactionRequestIsValid() {

        //given
        UUID cardId = stubTransactionService.fixedCardId;
        UUID adminId = stubTransactionService.fixedAdminId;

        CreateTransactionRequestDto requestDto = new CreateTransactionRequestDto();
        requestDto.setDirection(DirectionDto.IN);
        requestDto.setAmount(100);
        requestDto.setNote("Bonus points");

        UserDetails principal = adminPrincipal(adminId);

        //when
        ResponseEntity<TransactionDto> response = transactionController.createTransaction(cardId, requestDto, principal);

        //then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TransactionStatusDto.SUCCESS, response.getBody().getStatus());
    }

    @Test
    @DisplayName("createTransaction: returned transaction has the correct direction")
    void shouldReturnCorrectDirectionWhenTransactionIsCreated() {

        //given
        UUID cardId = stubTransactionService.fixedCardId;
        UUID adminId = stubTransactionService.fixedAdminId;

        CreateTransactionRequestDto requestDto = new CreateTransactionRequestDto();
        requestDto.setDirection(DirectionDto.OUT);
        requestDto.setAmount(50);

        UserDetails principal = adminPrincipal(adminId);

        //when
        ResponseEntity<TransactionDto> response = transactionController.createTransaction(cardId, requestDto, principal);

        //then
        assertNotNull(response.getBody());
        assertEquals(DirectionDto.OUT, response.getBody().getDirection());
    }

    @Test
    @DisplayName("createTransaction: returned transaction carries the provided amount")
    void shouldReturnCorrectAmountWhenTransactionIsCreated() {

        //given
        UUID cardId = stubTransactionService.fixedCardId;
        UUID adminId = stubTransactionService.fixedAdminId;

        CreateTransactionRequestDto requestDto = new CreateTransactionRequestDto();
        requestDto.setDirection(DirectionDto.IN);
        requestDto.setAmount(200);

        UserDetails principal = adminPrincipal(adminId);

        //when
        ResponseEntity<TransactionDto> response = transactionController.createTransaction(cardId, requestDto, principal);

        //then
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getAmount());
    }

    // -----------------------------------------------------------------------
    // GET /cards/{cardId}/transactions
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getTransactions: returns 200 OK with all transactions for the card")
    void shouldReturnOkWithListWhenGetAllTransactionsForCard() {

        //given
        UUID cardId = stubTransactionService.fixedCardId;

        //when
        ResponseEntity<List<TransactionDto>> response = transactionController.getTransactions(cardId);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("getTransactions: response list is not empty")
    void shouldReturnNonEmptyListWhenGetAllTransactionsForCard() {

        //given
        UUID cardId = stubTransactionService.fixedCardId;

        //when
        ResponseEntity<List<TransactionDto>> response = transactionController.getTransactions(cardId);

        //then
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    // -----------------------------------------------------------------------
    // GET /cards/{cardId}/transactions/{transactionId}
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getTransactionById: returns 200 OK with the correct transaction")
    void shouldReturnOkWithTransactionWhenTransactionIdIsValid() {

        //given
        UUID cardId = stubTransactionService.fixedCardId;
        UUID transactionId = stubTransactionService.fixedTransactionId;

        //when
        ResponseEntity<TransactionDto> response = transactionController.getTransactionById(cardId, transactionId);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(transactionId, response.getBody().getId());
    }

    // -----------------------------------------------------------------------
    // PUT /cards/{cardId}/transactions/{transactionId}/cancel
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("cancelTransaction: returns 200 OK and status is CANCELED")
    void shouldReturnCanceledStatusWhenTransactionIsCanceled() {

        //given
        UUID cardId = stubTransactionService.fixedCardId;
        UUID transactionId = stubTransactionService.fixedTransactionId;

        //when
        ResponseEntity<TransactionDto> response = transactionController.cancelTransaction(cardId, transactionId);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TransactionStatusDto.CANCELED, response.getBody().getStatus());
    }

    @Test
    @DisplayName("cancelTransaction: canceled transaction retains its original card ID")
    void shouldRetainCardIdWhenTransactionIsCanceled() {

        //given
        UUID cardId = stubTransactionService.fixedCardId;
        UUID transactionId = stubTransactionService.fixedTransactionId;

        //when
        ResponseEntity<TransactionDto> response = transactionController.cancelTransaction(cardId, transactionId);

        //then
        assertNotNull(response.getBody());
        assertEquals(cardId, response.getBody().getCardId());
    }
}
