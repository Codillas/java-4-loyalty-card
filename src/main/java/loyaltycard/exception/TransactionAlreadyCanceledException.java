package loyaltycard.exception;

import java.util.UUID;

public class TransactionAlreadyCanceledException extends RuntimeException {

    public TransactionAlreadyCanceledException(UUID transactionId) {
        super("Transaction is already canceled: " + transactionId);
    }
}
