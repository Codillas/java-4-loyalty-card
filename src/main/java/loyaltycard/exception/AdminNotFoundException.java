package loyaltycard.exception;

import java.util.UUID;

public class AdminNotFoundException extends RuntimeException {

    public AdminNotFoundException(UUID adminId) {
        super("Admin with id " + adminId + " was not found.");
    }
}
