package loyaltycard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import loyaltycard.controller.dto.CardDto;
import loyaltycard.controller.dto.StatusDto;
import loyaltycard.exception.CardNotFoundException;
import loyaltycard.exception.GlobalExceptionHandler;
import loyaltycard.mapper.CardMapper;
import loyaltycard.service.CardService;
import loyaltycard.service.model.Card;
import loyaltycard.service.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMVC тести для CardController.
 *
 * У Spring Boot 4.x @WebMvcTest більше не доступний, тому використовуємо
 * MockMvcBuilders.standaloneSetup(controller) — це ручне налаштування MockMvc
 * для одного контролера без завантаження Spring Context.
 *
 * MockMvc — дозволяє виконувати HTTP-запити до контролера без реального сервера
 *           і перевіряти статус, заголовки, тіло відповіді.
 */
@ExtendWith(MockitoExtension.class)
class CardControllerMockMvcTest {

    private MockMvc mockMvc;               // Головний інструмент для HTTP-запитів у тестах

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CardService cardService;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardController cardController;

    @BeforeEach
    void setUp() {
        // standaloneSetup — ручне налаштування MockMvc для одного контролера
        // setControllerAdvice — підключаємо GlobalExceptionHandler для обробки винятків
        mockMvc = MockMvcBuilders
                .standaloneSetup(cardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ---------------------------------------------------------------------------
    // Допоміжні методи
    // ---------------------------------------------------------------------------

    private Card buildCard(UUID cardId, UUID customerId, Status status) {
        Card card = new Card();
        card.setId(cardId);
        card.setCustomerId(customerId);
        card.setBalance(100);
        card.setStatus(status);
        card.setCreatedAt(Instant.now());
        card.setUpdatedAt(Instant.now());
        return card;
    }

    private CardDto buildCardDto(UUID cardId, UUID customerId, StatusDto status) {
        CardDto dto = new CardDto();
        dto.setId(cardId);
        dto.setCustomerId(customerId);
        dto.setBalance(100);
        dto.setStatusDto(status);
        dto.setCreatedAt(Instant.now());
        dto.setUpdatedAt(Instant.now());
        return dto;
    }

    // ===========================================================================
    // POST /cards/customers/{customerId}
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | POST /cards/customers/{customerId} → 201 CREATED")
    void shouldReturn201WhenAdminCreatesCard() throws Exception {

        // given
        UUID customerId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        Card card = buildCard(cardId, customerId, Status.ACTIVE);
        CardDto cardDto = buildCardDto(cardId, customerId, StatusDto.ACTIVE);

        // Налаштовуємо мок сервісу через when(…).thenReturn(…)
        when(cardService.createCard(customerId)).thenReturn(card);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        // when & then
        // perform() — виконати HTTP-запит
        // andExpect() — перевірити результат
        // jsonPath("$.поле") — перевірити значення поля в JSON
        mockMvc.perform(
                        post("/cards/customers/{customerId}", customerId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())                           // Перевіряємо статус 201
                .andExpect(jsonPath("$.id").value(cardId.toString()))      // Перевіряємо поле JSON
                .andExpect(jsonPath("$.statusDto").value("ACTIVE"))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()));

        // Перевіряємо, що сервіс справді викликався
        verify(cardService).createCard(customerId);
    }

    // ===========================================================================
    // GET /cards/{cardId}
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | GET /cards/{cardId} → 200 OK з карткою")
    void shouldReturn200WhenGettingCardById() throws Exception {

        UUID cardId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        Card card = buildCard(cardId, customerId, Status.ACTIVE);
        CardDto cardDto = buildCardDto(cardId, customerId, StatusDto.ACTIVE);

        when(cardService.getCardById(cardId)).thenReturn(card);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        mockMvc.perform(get("/cards/{cardId}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId.toString()))
                .andExpect(jsonPath("$.balance").value(100));

        verify(cardService).getCardById(cardId);
    }

    @Test
    @DisplayName("MockMVC | GET /cards/{cardId} → 404 якщо картка не знайдена")
    void shouldReturn404WhenCardNotFound() throws Exception {

        UUID cardId = UUID.randomUUID();
        // Налаштовуємо мок кинути виняток → GlobalExceptionHandler конвертує у 404
        when(cardService.getCardById(cardId)).thenThrow(new CardNotFoundException(cardId));

        mockMvc.perform(get("/cards/{cardId}", cardId))
                .andExpect(status().isNotFound());
    }

    // ===========================================================================
    // PUT /cards/{cardId}/activate
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | PUT /cards/{cardId}/activate → 200 OK зі статусом ACTIVE")
    void shouldReturn200WhenActivatingCard() throws Exception {

        UUID cardId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        Card card = buildCard(cardId, customerId, Status.ACTIVE);
        CardDto cardDto = buildCardDto(cardId, customerId, StatusDto.ACTIVE);

        when(cardService.activateCard(cardId)).thenReturn(card);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        mockMvc.perform(put("/cards/{cardId}/activate", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusDto").value("ACTIVE"));

        verify(cardService).activateCard(cardId);
    }

    // ===========================================================================
    // PUT /cards/{cardId}/block
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | PUT /cards/{cardId}/block → 200 OK зі статусом BLOCKED")
    void shouldReturn200WhenBlockingCard() throws Exception {

        UUID cardId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        Card card = buildCard(cardId, customerId, Status.BLOCKED);
        CardDto cardDto = buildCardDto(cardId, customerId, StatusDto.BLOCKED);

        when(cardService.blockCard(cardId)).thenReturn(card);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        mockMvc.perform(put("/cards/{cardId}/block", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusDto").value("BLOCKED"));

        verify(cardService).blockCard(cardId);
    }

    @Test
    @DisplayName("MockMVC | PUT /cards/{cardId}/block → 404 якщо картка не існує")
    void shouldReturn404WhenBlockingNonExistentCard() throws Exception {

        UUID cardId = UUID.randomUUID();
        when(cardService.blockCard(cardId)).thenThrow(new CardNotFoundException(cardId));

        mockMvc.perform(put("/cards/{cardId}/block", cardId))
                .andExpect(status().isNotFound());
    }
}
