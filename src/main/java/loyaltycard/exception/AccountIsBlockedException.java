package loyaltycard.exception;

public class AccountIsBlockedException extends RuntimeException {
    public AccountIsBlockedException() {
        super("Your account has been blocked.");
    }
}
