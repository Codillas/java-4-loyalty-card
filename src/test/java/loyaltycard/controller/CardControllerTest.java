package loyaltycard.controller;

import loyaltycard.controller.dto.CardDto;
import loyaltycard.controller.dto.StatusDto;
import loyaltycard.mapper.CardMapper;
import loyaltycard.service.CardService;
import loyaltycard.service.model.Card;
import loyaltycard.service.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CardControllerTest {

    // --- Stub implementation ---

    private static class StubCardService implements CardService {

        final UUID fixedCardId     = UUID.fromString("dddddddd-0000-0000-0000-000000000001");
        final UUID fixedCustomerId = UUID.fromString("cccccccc-0000-0000-0000-000000000001");

        private Card buildCard(Status status) {
            Card card = new Card();
            card.setId(fixedCardId);
            card.setCustomerId(fixedCustomerId);
            card.setBalance(150);
            card.setStatus(status);
            card.setCreatedAt(Instant.now());
            card.setUpdatedAt(Instant.now());
            return card;
        }

        @Override
        public Card createCard(UUID customerId) {
            return buildCard(Status.ACTIVE);
        }

        @Override
        public Card getCardById(UUID cardId) {
            return buildCard(Status.ACTIVE);
        }

        @Override
        public Card activateCard(UUID cardId) {
            return buildCard(Status.ACTIVE);
        }

        @Override
        public Card blockCard(UUID cardId) {
            return buildCard(Status.BLOCKED);
        }

        @Override
        public boolean isCardOwner(UUID cardId, String principalId) {
            return fixedCustomerId.toString().equals(principalId);
        }
    }

    private CardController cardController;
    private StubCardService stubCardService;

    @BeforeEach
    void setUp() {
        stubCardService = new StubCardService();
        CardMapper cardMapper = new CardMapper();
        cardController = new CardController(stubCardService, cardMapper);
    }

    // -----------------------------------------------------------------------
    // POST /cards/customers/{customerId}  (admin creates a card)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("createCard: returns 201 CREATED with card body")
    void shouldReturnCreatedWhenAdminCreatesCard() {

        //given
        UUID customerId = stubCardService.fixedCustomerId;

        //when
        ResponseEntity<CardDto> response = cardController.createCard(customerId);

        //then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(customerId, response.getBody().getCustomerId());
    }

    @Test
    @DisplayName("createCard: newly created card is ACTIVE")
    void shouldReturnActiveStatusWhenCardIsCreated() {

        //given
        UUID customerId = stubCardService.fixedCustomerId;

        //when
        ResponseEntity<CardDto> response = cardController.createCard(customerId);

        //then
        assertNotNull(response.getBody());
        assertEquals(StatusDto.ACTIVE, response.getBody().getStatusDto());
    }

    // -----------------------------------------------------------------------
    // GET /cards/{cardId}
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getCardById: returns 200 OK with correct card")
    void shouldReturnOkWithCardWhenCardIdIsValid() {

        //given
        UUID cardId = stubCardService.fixedCardId;

        //when
        ResponseEntity<CardDto> response = cardController.getCardById(cardId);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(cardId, response.getBody().getId());
    }

    @Test
    @DisplayName("getCardById: returned card has a non-negative balance")
    void shouldReturnNonNegativeBalanceWhenCardIsRetrieved() {

        //given
        UUID cardId = stubCardService.fixedCardId;

        //when
        ResponseEntity<CardDto> response = cardController.getCardById(cardId);

        //then
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getBalance() >= 0);
    }

    // -----------------------------------------------------------------------
    // PUT /cards/{cardId}/activate
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("activateCard: returns 200 OK and card status is ACTIVE")
    void shouldReturnActiveStatusWhenCardIsActivated() {

        //given
        UUID cardId = stubCardService.fixedCardId;

        //when
        ResponseEntity<CardDto> response = cardController.activateCard(cardId);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(StatusDto.ACTIVE, response.getBody().getStatusDto());
    }

    // -----------------------------------------------------------------------
    // PUT /cards/{cardId}/block
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("blockCard: returns 200 OK and card status is BLOCKED")
    void shouldReturnBlockedStatusWhenCardIsBlocked() {

        //given
        UUID cardId = stubCardService.fixedCardId;

        //when
        ResponseEntity<CardDto> response = cardController.blockCard(cardId);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(StatusDto.BLOCKED, response.getBody().getStatusDto());
    }
}
