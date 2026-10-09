package netcafe.exception;

public final class InactiveEmployeeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InactiveEmployeeException(String message) {
        super(message);
    }
}
