package loyaltycard.exception;

import java.util.UUID;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(UUID customerId) {
        super("Customer with id " + customerId + " was not found.");
    }
}
