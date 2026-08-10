package loyaltycard.exception;

import java.util.UUID;

public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(UUID cardId) {
        super("Card not found with id: " + cardId);
    }
}
