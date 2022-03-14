/**
 * Exception to throw when the field of data point object is not found.
 */
public class NotFound extends Exception {
    /**
     * Default constructor.
     */
    public NotFound() {
        super("Data entry not found!");
    }

    /**
     * Parameterised constructor setting the message.
     *
     * @param message Message to be used for the exception.
     */
    public NotFound(String message) {
        super(message);
    }

    /**
     * Parameterised constructor setting the message and throwable.
     *
     * @param message Message to be used for the exception.
     * @param throwable Throwable exception.
     */
    public NotFound(String message, Throwable throwable) {
        super(message, throwable);
    }
}
