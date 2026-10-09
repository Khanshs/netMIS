package netcafe.exception;

public final class PaymentAmountMismatchException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PaymentAmountMismatchException(String message) {
        super(message);
    }
}
