package com.alerts;
//Interface for alerts
public interface Alert {
    String getPatientId();
    String getCondition();
    long getTimestamp();
}
