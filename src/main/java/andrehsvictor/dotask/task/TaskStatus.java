package andrehsvictor.dotask.task;

public enum TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public static TaskStatus fromString(String status) {
        return TaskStatus.valueOf(status.toUpperCase());
    }
}
