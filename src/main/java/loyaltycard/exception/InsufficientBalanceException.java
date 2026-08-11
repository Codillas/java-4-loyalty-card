package loyaltycard.exception;

import java.util.UUID;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(UUID cardId) {
        super("Card with id " + cardId + " has insufficient balance for this transaction.");
    }
}
