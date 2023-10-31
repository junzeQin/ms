package io.metersphere.commons.exception;

public class MSException extends RuntimeException {

    private Object detail;

    public MSException(String message) {
        super(message);
    }

    public MSException(String message, Object detail) {
        super(message);
        this.detail = detail;
    }

    public MSException(Throwable t) {
        super(t);
    }

    public MSException(Throwable t, Object detail) {
        super(t);
        this.detail = detail;
    }

    public static void throwException(String message) {
        throw new MSException(message);
    }

    public static void throwException(String message, Object detail) {
        throw new MSException(message, detail);
    }

    public static MSException getException(String message) {
        throw new MSException(message);
    }

    public static void throwException(Throwable t) {
        throw new MSException(t);
    }

    public static void throwException(Throwable t, Object detail) {
        throw new MSException(t, detail);
    }

    public Object getDetail() {
        return detail;
    }
}
