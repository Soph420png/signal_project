package com.alerts;

public class PriorityAlertDecorator extends AlertDecorator {
    private final int priority;

    public PriorityAlertDecorator(Alert inner, int priority) {
        super(inner);
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}

