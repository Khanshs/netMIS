package netcafe.exception;

public final class DuplicateIdException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DuplicateIdException(String message) {
        super(message);
    }
}
