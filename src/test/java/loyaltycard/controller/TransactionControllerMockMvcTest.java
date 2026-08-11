package loyaltycard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import loyaltycard.controller.dto.CreateTransactionRequestDto;
import loyaltycard.controller.dto.DirectionDto;
import loyaltycard.controller.dto.TransactionDto;
import loyaltycard.controller.dto.TransactionStatusDto;
import loyaltycard.exception.CardIsBlockedException;
import loyaltycard.exception.GlobalExceptionHandler;
import loyaltycard.exception.InsufficientBalanceException;
import loyaltycard.exception.TransactionNotFoundException;
import loyaltycard.mapper.TransactionMapper;
import loyaltycard.service.TransactionService;
import loyaltycard.service.model.Direction;
import loyaltycard.service.model.Transaction;
import loyaltycard.service.model.TransactionStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMVC тести для TransactionController.
 *
 * Особливість: контролер використовує @AuthenticationPrincipal UserDetails,
 * тому нам треба встановити SecurityContext вручну через SecurityContextHolder.
 * Це дозволяє @AuthenticationPrincipalArgumentResolver знайти Principal.
 */
@ExtendWith(MockitoExtension.class)
class TransactionControllerMockMvcTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionController transactionController;

    // UUID адміна, який буде в Principal
    private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        // standaloneSetup + AuthenticationPrincipalArgumentResolver
        // — це дозволяє @AuthenticationPrincipal працювати у standaloneSetup
        mockMvc = MockMvcBuilders
                .standaloneSetup(transactionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        // SecurityContextHolder.setContext() — встановлюємо Principal вручну
        // замість @WithMockUser (який вимагає Spring Context)
        User adminUser = new User(
                ADMIN_ID.toString(),
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities());

        // Встановлюємо SecurityContext щоб @AuthenticationPrincipal міг отримати UserDetails
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        // Очищуємо SecurityContext після кожного тесту щоб не було витоку між тестами
        SecurityContextHolder.clearContext();
    }

    // ---------------------------------------------------------------------------
    // Допоміжний метод
    // ---------------------------------------------------------------------------

    private Transaction buildTransaction(UUID txId, UUID cardId, Direction direction,
                                         int amount, TransactionStatus status) {
        Transaction tx = new Transaction();
        tx.setId(txId);
        tx.setAdminId(ADMIN_ID);
        tx.setCardId(cardId);
        tx.setDirection(direction);
        tx.setAmount(amount);
        tx.setStatus(status);
        tx.setNote("test note");
        tx.setCreatedAt(Instant.now());
        tx.setUpdatedAt(Instant.now());
        return tx;
    }

    private TransactionDto buildTransactionDto(UUID txId, UUID cardId, DirectionDto direction,
                                               int amount, TransactionStatusDto status) {
        TransactionDto dto = new TransactionDto();
        dto.setId(txId);
        dto.setAdminId(ADMIN_ID);
        dto.setCardId(cardId);
        dto.setDirection(direction);
        dto.setAmount(amount);
        dto.setStatus(status);
        dto.setNote("test note");
        dto.setCreatedAt(Instant.now());
        dto.setUpdatedAt(Instant.now());
        return dto;
    }

    // ===========================================================================
    // POST /cards/{cardId}/transactions
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | POST /cards/{cardId}/transactions → 201 CREATED (IN-транзакція)")
    void shouldReturn201WhenCreatingTransaction() throws Exception {

        // given
        UUID cardId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        CreateTransactionRequestDto requestDto = new CreateTransactionRequestDto();
        requestDto.setDirection(DirectionDto.IN);
        requestDto.setAmount(100);
        requestDto.setNote("Bonus points");

        Transaction createdTx = buildTransaction(txId, cardId, Direction.IN, 100, TransactionStatus.SUCCESS);
        TransactionDto txDto = buildTransactionDto(txId, cardId, DirectionDto.IN, 100, TransactionStatusDto.SUCCESS);

        // any(UUID.class) — матчер для будь-якого UUID
        when(transactionService.createTransaction(eq(cardId), any(Transaction.class), eq(ADMIN_ID)))
                .thenReturn(createdTx);
        when(transactionMapper.toDto(createdTx)).thenReturn(txDto);

        // when & then
        // perform() — виконати HTTP-запит
        // andExpect() — перевірити результат
        mockMvc.perform(
                        post("/cards/{cardId}/transactions", cardId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(txId.toString()))
                .andExpect(jsonPath("$.direction").value("IN"))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(transactionService).createTransaction(eq(cardId), any(Transaction.class), eq(ADMIN_ID));
    }

    @Test
    @DisplayName("MockMVC | POST /cards/{cardId}/transactions → 400 якщо картку заблоковано")
    void shouldReturn400WhenCardIsBlocked() throws Exception {

        UUID cardId = UUID.randomUUID();

        CreateTransactionRequestDto requestDto = new CreateTransactionRequestDto();
        requestDto.setDirection(DirectionDto.IN);
        requestDto.setAmount(100);
        requestDto.setNote("test");

        // thenThrow — GlobalExceptionHandler конвертує у 400
        when(transactionService.createTransaction(any(), any(), any()))
                .thenThrow(new CardIsBlockedException(cardId));

        mockMvc.perform(
                        post("/cards/{cardId}/transactions", cardId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("MockMVC | POST /cards/{cardId}/transactions → 400 при недостатньому балансі")
    void shouldReturn400WhenInsufficientBalance() throws Exception {

        UUID cardId = UUID.randomUUID();

        CreateTransactionRequestDto requestDto = new CreateTransactionRequestDto();
        requestDto.setDirection(DirectionDto.OUT);
        requestDto.setAmount(9999);
        requestDto.setNote("test");

        when(transactionService.createTransaction(any(), any(), any()))
                .thenThrow(new InsufficientBalanceException(cardId));

        mockMvc.perform(
                        post("/cards/{cardId}/transactions", cardId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    // ===========================================================================
    // GET /cards/{cardId}/transactions
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | GET /cards/{cardId}/transactions → 200 OK зі списком транзакцій")
    void shouldReturn200WithTransactionListForCard() throws Exception {

        UUID cardId = UUID.randomUUID();
        UUID txId1 = UUID.randomUUID();
        UUID txId2 = UUID.randomUUID();

        Transaction tx1 = buildTransaction(txId1, cardId, Direction.IN, 100, TransactionStatus.SUCCESS);
        Transaction tx2 = buildTransaction(txId2, cardId, Direction.OUT, 50, TransactionStatus.SUCCESS);
        TransactionDto dto1 = buildTransactionDto(txId1, cardId, DirectionDto.IN, 100, TransactionStatusDto.SUCCESS);
        TransactionDto dto2 = buildTransactionDto(txId2, cardId, DirectionDto.OUT, 50, TransactionStatusDto.SUCCESS);

        when(transactionService.getTransactions(cardId)).thenReturn(List.of(tx1, tx2));
        when(transactionMapper.toDto(tx1)).thenReturn(dto1);
        when(transactionMapper.toDto(tx2)).thenReturn(dto2);

        // jsonPath("$.length()") — перевіряємо розмір масиву
        // jsonPath("$[0].direction") — перший елемент масиву, поле direction
        mockMvc.perform(get("/cards/{cardId}/transactions", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].direction").value("IN"))
                .andExpect(jsonPath("$[1].direction").value("OUT"));
    }

    // ===========================================================================
    // GET /cards/{cardId}/transactions/{transactionId}
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | GET /cards/{cardId}/transactions/{transactionId} → 200 OK")
    void shouldReturn200WhenGettingTransactionById() throws Exception {

        UUID cardId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        Transaction tx = buildTransaction(txId, cardId, Direction.IN, 100, TransactionStatus.SUCCESS);
        TransactionDto dto = buildTransactionDto(txId, cardId, DirectionDto.IN, 100, TransactionStatusDto.SUCCESS);

        when(transactionService.getTransactionById(cardId, txId)).thenReturn(tx);
        when(transactionMapper.toDto(tx)).thenReturn(dto);

        mockMvc.perform(get("/cards/{cardId}/transactions/{transactionId}", cardId, txId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(txId.toString()))
                .andExpect(jsonPath("$.amount").value(100));
    }

    @Test
    @DisplayName("MockMVC | GET /cards/{cardId}/transactions/{transactionId} → 404 якщо не знайдено")
    void shouldReturn404WhenTransactionNotFound() throws Exception {

        UUID cardId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        when(transactionService.getTransactionById(cardId, txId))
                .thenThrow(new TransactionNotFoundException(txId));

        mockMvc.perform(get("/cards/{cardId}/transactions/{transactionId}", cardId, txId))
                .andExpect(status().isNotFound());
    }

    // ===========================================================================
    // PUT /cards/{cardId}/transactions/{transactionId}/cancel
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | PUT /cards/{cardId}/transactions/{transactionId}/cancel → 200 OK")
    void shouldReturn200WhenCancelingTransaction() throws Exception {

        UUID cardId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        Transaction canceledTx = buildTransaction(txId, cardId, Direction.IN, 100, TransactionStatus.CANCELED);
        TransactionDto canceledDto = buildTransactionDto(txId, cardId, DirectionDto.IN, 100, TransactionStatusDto.CANCELED);

        when(transactionService.cancelTransaction(cardId, txId)).thenReturn(canceledTx);
        when(transactionMapper.toDto(canceledTx)).thenReturn(canceledDto);

        mockMvc.perform(put("/cards/{cardId}/transactions/{transactionId}/cancel", cardId, txId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        verify(transactionService).cancelTransaction(cardId, txId);
    }

    @Test
    @DisplayName("MockMVC | PUT /cards/{cardId}/transactions/{transactionId}/cancel → 404 якщо транзакція не знайдена")
    void shouldReturn404WhenCancelingNonExistentTransaction() throws Exception {

        UUID cardId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        when(transactionService.cancelTransaction(cardId, txId))
                .thenThrow(new TransactionNotFoundException(txId));

        mockMvc.perform(put("/cards/{cardId}/transactions/{transactionId}/cancel", cardId, txId))
                .andExpect(status().isNotFound());
    }
}
