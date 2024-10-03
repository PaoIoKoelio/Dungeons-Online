package bg.sofia.uni.fmi.mjt.dungeons.exception;

public class NoSuchItemException extends Exception {
    public NoSuchItemException(String message) {
        super(message);
    }

    public NoSuchItemException(String message, Throwable cause) {
        super(message, cause);
    }
}
