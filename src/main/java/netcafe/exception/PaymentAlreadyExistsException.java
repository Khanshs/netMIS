package netcafe.exception;

public final class PaymentAlreadyExistsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PaymentAlreadyExistsException(String message) {
        super(message);
    }
}
