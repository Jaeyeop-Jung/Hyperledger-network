package exception;

public class CoinNotFoundException extends RuntimeException{
    public CoinNotFoundException(String message) {
        super(message);
    }
}
