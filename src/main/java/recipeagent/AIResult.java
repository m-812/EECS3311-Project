package recipeagent;

public class AIResult<T> {
    private T data;
    private String message;
    private boolean flagged;
    private boolean success;

    private AIResult(T data, String message, boolean flagged, boolean success) {
        this.data = data;
        this.message = message;
        this.flagged = flagged;
        this.success = success;
    }

    public static <T> AIResult<T> success(T data, String message) {
        return new AIResult<>(data, message, false, true);
    }

    public static <T> AIResult<T> flagged(String message) {
        return new AIResult<>(null, message, true, false);
    }

    public static <T> AIResult<T> error(String message) {
        return new AIResult<>(null, message, false, false);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
