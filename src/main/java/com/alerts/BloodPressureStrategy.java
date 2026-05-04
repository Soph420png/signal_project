package com.alerts;

import com.data_management.PatientRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BloodPressureStrategy implements AlertStrategy {

    @Override
    public void checkAlert(List<PatientRecord> records, Consumer<Alert> alertSink) {
        if (records ==null||records.isEmpty()||alertSink== null) {
            return;
        }

        List<PatientRecord> sys = filterByType(records, "SystolicPressure");
        List<PatientRecord> dia = filterByType(records, "DiastolicPressure");

        for (PatientRecord r : sys) {
            if (r.getMeasurementValue() > 180.0 || r.getMeasurementValue() < 90.0) {
                alertSink.accept(AlertFactory.create(Integer.toString(r.getPatientId()),
                        "CRITICAL_SYSTOLIC_PRESSURE", r.getTimestamp()));
            }
        }
        for (PatientRecord r : dia) {
            if (r.getMeasurementValue() > 120.0 || r.getMeasurementValue() < 60.0) {
                alertSink.accept(AlertFactory.create(Integer.toString(r.getPatientId()),
                        "CRITICAL_DIASTOLIC_PRESSURE", r.getTimestamp()));
            }
        }

        if (hasTrend(sys)) {
            PatientRecord last = sys.get(sys.size() - 1);
            alertSink.accept(AlertFactory.create(Integer.toString(last.getPatientId()),
                    "SYSTOLIC_TREND_ALERT", last.getTimestamp()));
        }
        if (hasTrend(dia)) {
            PatientRecord last = dia.get(dia.size() - 1);
            alertSink.accept(AlertFactory.create(Integer.toString(last.getPatientId()),
                    "DIASTOLIC_TREND_ALERT", last.getTimestamp()));
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

    private static boolean hasTrend(List<PatientRecord> records) {
        if (records.size() < 3) {
            return false;
        }
        for (int i = 0; i + 2 < records.size(); i++) {
            double a = records.get(i).getMeasurementValue();
            double b = records.get(i + 1).getMeasurementValue();
            double c = records.get(i + 2).getMeasurementValue();

            double d1 = b-a;
            double d2 = c-b;
            if (Math.abs(d1) <= 10.0 || Math.abs(d2) <= 10.0) {
                continue;
            }
            if ((d1 > 0 && d2 > 0) || (d1 < 0 && d2 < 0)) {
                return true;
            }
        }
        return false;
    }
}