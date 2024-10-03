package bg.sofia.uni.fmi.mjt.dungeons.exception;

public class GameIsFullException extends Exception {
    public GameIsFullException(String message) {
        super(message);
    }

    public GameIsFullException(String message, Throwable cause) {
        super(message, cause);
    }
}
