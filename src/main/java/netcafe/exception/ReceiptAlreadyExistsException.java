package netcafe.exception;

public final class ReceiptAlreadyExistsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ReceiptAlreadyExistsException(String message) {
        super(message);
    }
}
