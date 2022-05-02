package exception;

public class NotEnoughCoinValueException extends RuntimeException{
    public NotEnoughCoinValueException(String message) {
        super(message);
    }
}
