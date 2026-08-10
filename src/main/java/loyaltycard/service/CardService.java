package loyaltycard.service;

import loyaltycard.service.model.Card;

import java.util.UUID;

public interface CardService {

    Card createCard(UUID customerId);

    Card getCardById(UUID cardId);

    Card activateCard(UUID cardId);

    Card blockCard(UUID cardId);

    boolean isCardOwner(UUID cardId, String principalId);
}
