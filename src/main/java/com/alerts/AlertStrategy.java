package com.alerts;

import com.data_management.PatientRecord;
import java.util.List;
import java.util.function.Consumer;

//Interface for alert strategies
public interface AlertStrategy {
    void checkAlert(List<PatientRecord> records, Consumer<Alert> alertSink);
}

