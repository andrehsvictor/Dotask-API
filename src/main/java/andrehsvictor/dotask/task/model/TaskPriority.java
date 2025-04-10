package andrehsvictor.dotask.task.model;

public enum TaskPriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int value;

    TaskPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TaskPriority fromValue(int value) {
        for (TaskPriority priority : TaskPriority.values()) {
            if (priority.getValue() == value) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Invalid priority value: " + value);
    }

    public static TaskPriority fromString(String priority) {
        switch (priority.toLowerCase()) {
            case "low":
                return LOW;
            case "medium":
                return MEDIUM;
            case "high":
                return HIGH;
            default:
                throw new IllegalArgumentException("Invalid priority string: " + priority);
        }
    }
}
