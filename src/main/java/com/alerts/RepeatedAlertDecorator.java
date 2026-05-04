package com.alerts;

public class RepeatedAlertDecorator extends AlertDecorator {
    private final long repeatIntervalMs;

    public RepeatedAlertDecorator(Alert inner, long repeatIntervalMs) {
        super(inner);
        this.repeatIntervalMs = repeatIntervalMs;
    }
    public long getRepeatIntervalMs() {
        return repeatIntervalMs;
    }
}

