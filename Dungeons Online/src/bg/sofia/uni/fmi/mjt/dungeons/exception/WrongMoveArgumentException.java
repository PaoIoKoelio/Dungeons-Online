package bg.sofia.uni.fmi.mjt.dungeons.exception;

public class WrongMoveArgumentException extends Exception {
    public WrongMoveArgumentException(String message) {
        super(message);
    }

    public WrongMoveArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
