package loyaltycard.service;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-тести для CardServiceImpl.
 * Використовує Mockito для мокування залежностей (CardRepository, CustomerRepository, CardMapper).
 */
@ExtendWith(MockitoExtension.class)        // Підключає Mockito-розширення JUnit 5
class CardServiceImplTest {

    // ---------------------------------------------------------------------------
    // Поля з анотаціями Mockito
    // ---------------------------------------------------------------------------

    @Mock                                   // Створює мок-об'єкт (підробну реалізацію)
    private CardRepository cardRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks                            // Створює реальний екземпляр і вставляє @Mock-и через конструктор/setter
    private CardServiceImpl cardService;

    // ---------------------------------------------------------------------------
    // Допоміжний метод: побудова CardEntity
    // ---------------------------------------------------------------------------

    private CardEntity buildCardEntity(UUID cardId, UUID customerId, StatusEntity status) {
        CardEntity entity = new CardEntity();
        entity.setId(cardId);
        entity.setCustomerId(customerId);
        entity.setBalance(0);
        entity.setStatusEntity(status);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    private Card buildCard(UUID cardId, UUID customerId, Status status) {
        Card card = new Card();
        card.setId(cardId);
        card.setCustomerId(customerId);
        card.setBalance(0);
        card.setStatus(status);
        card.setCreatedAt(Instant.now());
        card.setUpdatedAt(Instant.now());
        return card;
    }

    // ===========================================================================
    // createCard()
    // ===========================================================================

    @Test
    @DisplayName("createCard: повертає картку, якщо клієнт існує та картки ще немає")
    void shouldCreateCardSuccessfully() {

        // given
        UUID customerId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        CardEntity savedEntity = buildCardEntity(cardId, customerId, StatusEntity.ACTIVE);
        Card expectedCard = buildCard(cardId, customerId, Status.ACTIVE);

        // when(…).thenReturn(…) — налаштовуємо мок: при виклику методу повертаємо задане значення
        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(cardRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());
        when(cardMapper.toEntity(any(Card.class))).thenReturn(savedEntity);
        when(cardRepository.save(any(CardEntity.class))).thenReturn(savedEntity);
        when(cardMapper.toDomain(savedEntity)).thenReturn(expectedCard);

        // when
        Card result = cardService.createCard(customerId);

        // then
        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        assertEquals(Status.ACTIVE, result.getStatus());

        // verify(…) — перевіряємо, що метод мока справді був викликаний
        verify(cardRepository).save(any(CardEntity.class));
    }

    @Test
    @DisplayName("createCard: кидає CustomerNotFoundException, якщо клієнта не існує")
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {

        // given
        UUID customerId = UUID.randomUUID();
        when(customerRepository.existsById(customerId)).thenReturn(false);

        // then — assertThrows перевіряє, що виняток дійсно кидається
        assertThrows(CustomerNotFoundException.class, () -> cardService.createCard(customerId));

        // Переконуємося, що save ніколи не викликався
        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCard: кидає CardAlreadyExistsException, якщо картка вже є")
    void shouldThrowCardAlreadyExistsExceptionWhenCardAlreadyExists() {

        // given
        UUID customerId = UUID.randomUUID();
        CardEntity existingCard = buildCardEntity(UUID.randomUUID(), customerId, StatusEntity.ACTIVE);

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(cardRepository.findByCustomerId(customerId)).thenReturn(Optional.of(existingCard));

        // then
        assertThrows(CardAlreadyExistsException.class, () -> cardService.createCard(customerId));
        verify(cardRepository, never()).save(any());
    }

    // ===========================================================================
    // getCardById()
    // ===========================================================================

    @Test
    @DisplayName("getCardById: повертає картку, якщо вона існує")
    void shouldReturnCardWhenCardExists() {

        // given
        UUID cardId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        CardEntity entity = buildCardEntity(cardId, customerId, StatusEntity.ACTIVE);
        Card expectedCard = buildCard(cardId, customerId, Status.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(entity));
        when(cardMapper.toDomain(entity)).thenReturn(expectedCard);

        // when
        Card result = cardService.getCardById(cardId);

        // then
        assertNotNull(result);
        assertEquals(cardId, result.getId());
    }

    @Test
    @DisplayName("getCardById: кидає CardNotFoundException, якщо картки не існує")
    void shouldThrowCardNotFoundExceptionWhenCardDoesNotExist() {

        // given
        UUID cardId = UUID.randomUUID();
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // then
        assertThrows(CardNotFoundException.class, () -> cardService.getCardById(cardId));
    }

    // ===========================================================================
    // activateCard()
    // ===========================================================================

    @Test
    @DisplayName("activateCard: встановлює статус ACTIVE і зберігає картку")
    void shouldActivateCardSuccessfully() {

        // given
        UUID cardId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        CardEntity entity = buildCardEntity(cardId, customerId, StatusEntity.BLOCKED);
        CardEntity savedEntity = buildCardEntity(cardId, customerId, StatusEntity.ACTIVE);
        Card expectedCard = buildCard(cardId, customerId, Status.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(entity));
        when(cardRepository.save(entity)).thenReturn(savedEntity);
        when(cardMapper.toDomain(savedEntity)).thenReturn(expectedCard);

        // when
        Card result = cardService.activateCard(cardId);

        // then
        assertEquals(Status.ACTIVE, result.getStatus());
        // Перевіряємо, що встановлено правильний статус перед збереженням
        verify(cardRepository).save(entity);
        assertEquals(StatusEntity.ACTIVE, entity.getStatusEntity());
    }

    @Test
    @DisplayName("activateCard: кидає CardNotFoundException, якщо картки не існує")
    void shouldThrowWhenActivatingNonExistentCard() {

        UUID cardId = UUID.randomUUID();
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.activateCard(cardId));
    }

    // ===========================================================================
    // blockCard()
    // ===========================================================================

    @Test
    @DisplayName("blockCard: встановлює статус BLOCKED і зберігає картку")
    void shouldBlockCardSuccessfully() {

        // given
        UUID cardId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        CardEntity entity = buildCardEntity(cardId, customerId, StatusEntity.ACTIVE);
        CardEntity savedEntity = buildCardEntity(cardId, customerId, StatusEntity.BLOCKED);
        Card expectedCard = buildCard(cardId, customerId, Status.BLOCKED);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(entity));
        when(cardRepository.save(entity)).thenReturn(savedEntity);
        when(cardMapper.toDomain(savedEntity)).thenReturn(expectedCard);

        // when
        Card result = cardService.blockCard(cardId);

        // then
        assertEquals(Status.BLOCKED, result.getStatus());
        verify(cardRepository).save(entity);
        assertEquals(StatusEntity.BLOCKED, entity.getStatusEntity());
    }

    @Test
    @DisplayName("blockCard: кидає CardNotFoundException, якщо картки не існує")
    void shouldThrowWhenBlockingNonExistentCard() {

        UUID cardId = UUID.randomUUID();
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.blockCard(cardId));
    }

    // ===========================================================================
    // isCardOwner()
    // ===========================================================================

    @Test
    @DisplayName("isCardOwner: повертає true, якщо ownerId співпадає")
    void shouldReturnTrueWhenPrincipalIsCardOwner() {

        UUID cardId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        CardEntity entity = buildCardEntity(cardId, customerId, StatusEntity.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(entity));

        boolean result = cardService.isCardOwner(cardId, customerId.toString());

        assertTrue(result);
    }

    @Test
    @DisplayName("isCardOwner: повертає false, якщо картки не існує")
    void shouldReturnFalseWhenCardDoesNotExistForOwnerCheck() {

        UUID cardId = UUID.randomUUID();
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        boolean result = cardService.isCardOwner(cardId, UUID.randomUUID().toString());

        assertFalse(result);
    }
}
