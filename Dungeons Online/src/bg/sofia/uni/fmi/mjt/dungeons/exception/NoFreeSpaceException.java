package bg.sofia.uni.fmi.mjt.dungeons.exception;

public class NoFreeSpaceException extends Exception {
    public NoFreeSpaceException(String message) {
        super(message);
    }

    public NoFreeSpaceException(String message, Throwable cause) {
        super(message, cause);
    }
}
