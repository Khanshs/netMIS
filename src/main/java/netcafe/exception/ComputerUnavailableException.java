package netcafe.exception;

public final class ComputerUnavailableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ComputerUnavailableException(String message) {
        super(message);
    }
}
