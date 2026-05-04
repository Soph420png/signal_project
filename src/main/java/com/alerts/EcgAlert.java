package com.alerts;

public class EcgAlert extends BasicAlert {
    public EcgAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }
}
