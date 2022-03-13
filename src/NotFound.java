public class NotFound extends Exception {
    public NotFound() {
        super("Data entry not found!");
    }

    public NotFound(String message) {
        super(message);
    }

    public NotFound(String message, Throwable throwable) {
        super(message, throwable);
    }
}
