package loyaltycard.exception;

import java.util.UUID;

public class CardAlreadyExistsException extends RuntimeException {

    public CardAlreadyExistsException(UUID customerId) {
        super("Card already exists for customer with id " + customerId);
    }
}
