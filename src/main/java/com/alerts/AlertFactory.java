package com.alerts;

import java.util.HashMap;
import java.util.Map;

public abstract class AlertFactory {
    private static final Map<String, AlertFactory> BY_CONDITION = new HashMap<>();

    static {
        register(new BloodPressureAlertFactory(), "CRITICAL_SYSTOLIC_PRESSURE", "CRITICAL_DIASTOLIC_PRESSURE",
                "SYSTOLIC_TREND_ALERT", "DIASTOLIC_TREND_ALERT", "MANUAL_ALERT_TRIGGERED");
        register(new BloodOxygenAlertFactory(), "LOW_SATURATION", "RAPID_SATURATION_DROP", "HYPOTENSIVE_HYPOXEMIA");
        register(new ECGAlertFactory(), "ABNORMAL_ECG_PEAK");
    }

    private static void register(AlertFactory factory, String... conditions) {
        for (String c : conditions) {
            BY_CONDITION.put(c, factory);
        }
    }

    public static Alert create(String patientId, String condition, long timestamp) {
        AlertFactory f = BY_CONDITION.get(condition);
        if (f == null) {
            throw new IllegalArgumentException("No factory for condition: " + condition);
        }
        return f.createAlert(patientId, condition, timestamp);
    }

    public abstract Alert createAlert(String patientId, String condition, long timestamp);
}
