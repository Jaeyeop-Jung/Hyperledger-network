package exception;

public class AlreadyExistsCoinException extends RuntimeException{
    public AlreadyExistsCoinException(String message) {
        super(message);
    }
}
