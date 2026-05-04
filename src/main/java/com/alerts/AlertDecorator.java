package com.alerts;

import java.util.Objects;

public abstract class AlertDecorator implements Alert {
    protected final Alert inner;

    protected AlertDecorator(Alert inner) {
        this.inner = Objects.requireNonNull(inner, "inner");
    }

    @Override
    public String getPatientId() {
        return inner.getPatientId();
    }

    @Override
    public String getCondition() {
        return inner.getCondition();
    }

    @Override
    public long getTimestamp() {
        return inner.getTimestamp();
    }
}

