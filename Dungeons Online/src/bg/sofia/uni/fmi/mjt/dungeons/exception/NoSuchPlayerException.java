package bg.sofia.uni.fmi.mjt.dungeons.exception;

public class NoSuchPlayerException extends Exception {
    public NoSuchPlayerException(String message) {
        super(message);
    }

    public NoSuchPlayerException(String message, Throwable cause) {
        super(message, cause);
    }
}
