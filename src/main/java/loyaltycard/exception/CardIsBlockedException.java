package loyaltycard.exception;

import java.util.UUID;

public class CardIsBlockedException extends RuntimeException {

    public CardIsBlockedException(UUID cardId) {
        super("Card is blocked: " + cardId);
    }
}
