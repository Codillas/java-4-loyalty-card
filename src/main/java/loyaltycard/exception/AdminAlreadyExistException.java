package loyaltycard.exception;

public class AdminAlreadyExistException extends RuntimeException {

    public AdminAlreadyExistException(String email) {
        super("Admin with email " + email + " already exists.");
    }
}
