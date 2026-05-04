package com.alerts;

import com.data_management.PatientRecord;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

//Evaluates ecg records to trigger alerts
public class HeartRateStrategy implements AlertStrategy {
    @Override
    public void checkAlert(List<PatientRecord> records, Consumer<Alert> alertSink) {
        if (records == null || records.isEmpty() || alertSink == null) {
            return;
        }

        List<PatientRecord> ecg = filterByType(records, "ECG");
        if (ecg.size() < 10) {
            return;
        }

        int window = 30;
        Deque<Double> q = new ArrayDeque<>(window);
        double sum = 0.0;
        double sumSq = 0.0;

        for (PatientRecord r : ecg) {
            double v = r.getMeasurementValue();
            if (q.size() == window) {
                double old = q.removeFirst();
                sum -= old;
                sumSq -= old * old;
            }
            q.addLast(v);
            sum += v;
            sumSq += v * v;

            if (q.size() < 10) {
                continue;
            }

            double mean = sum / q.size();
            double variance = (sumSq / q.size()) - (mean * mean);
            double std = variance > 0 ? Math.sqrt(variance) : 0.0;

            if (std > 0.0001 && Math.abs(v - mean) > 3.5 * std) {
                alertSink.accept(AlertFactory.create(Integer.toString(r.getPatientId()),
                        "ABNORMAL_ECG_PEAK", r.getTimestamp()));
                return;
            }
        }
    }
    private static List<PatientRecord> filterByType(List<PatientRecord> all, String recordType) {
        List<PatientRecord> out = new ArrayList<>();
        for (PatientRecord r : all) {
            if (recordType.equals(r.getRecordType())) {
                out.add(r);
            }
        }
        return out;
    }
}

