package exception;

public class AlreadyExistAssetException extends RuntimeException {
    public AlreadyExistAssetException(String message) {
        super(message);
    }
}
