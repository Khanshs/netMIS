package netcafe.exception;

public final class ActiveSessionExistsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ActiveSessionExistsException(String message) {
        super(message);
    }
}
