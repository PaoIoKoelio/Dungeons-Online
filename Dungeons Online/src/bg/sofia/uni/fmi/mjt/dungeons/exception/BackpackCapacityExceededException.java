package bg.sofia.uni.fmi.mjt.dungeons.exception;

public class BackpackCapacityExceededException extends Exception {
    public BackpackCapacityExceededException(String message) {
        super(message);
    }

    public BackpackCapacityExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
