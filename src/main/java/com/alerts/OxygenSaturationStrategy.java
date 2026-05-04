package com.alerts;

import com.data_management.PatientRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class OxygenSaturationStrategy implements AlertStrategy {
    @Override
    public void checkAlert(List<PatientRecord> records, Consumer<Alert> alertSink) {
        if (records == null || records.isEmpty() || alertSink == null) {
            return;
        }

        List<PatientRecord> sat = filterByType(records, "Saturation");
        if (!sat.isEmpty()) {
            for (PatientRecord r : sat) {
                if (r.getMeasurementValue() < 92.0) {
                    alertSink.accept(AlertFactory.create(Integer.toString(r.getPatientId()),
                            "LOW_SATURATION", r.getTimestamp()));
                }
            }

            final long tenMinutesMs = 10L * 60L * 1000L;
            int i = 0;
            for (int j = 0; j < sat.size(); j++) {
                while (sat.get(j).getTimestamp() - sat.get(i).getTimestamp() > tenMinutesMs) {
                    i++;
                }
                for (int k = i; k < j; k++) {
                    double drop = sat.get(k).getMeasurementValue() - sat.get(j).getMeasurementValue();
                    if (drop >= 5.0) {
                        alertSink.accept(AlertFactory.create(Integer.toString(sat.get(j).getPatientId()),
                                "RAPID_SATURATION_DROP", sat.get(j).getTimestamp()));
                        break;
                    }
                }
            }
        }

        List<PatientRecord> sys = filterByType(records, "SystolicPressure");
        if (sys.isEmpty() || sat.isEmpty()) {
            return;
        }

        final long tenMinutesMs = 10L * 60L * 1000L;
        for (PatientRecord s : sys) {
            if (s.getMeasurementValue() >= 90.0) {
                continue;
            }
            long t = s.getTimestamp();
            for (PatientRecord o : sat) {
                if (Math.abs(o.getTimestamp() - t) <= tenMinutesMs && o.getMeasurementValue() < 92.0) {
                    alertSink.accept(AlertFactory.create(Integer.toString(s.getPatientId()),
                            "HYPOTENSIVE_HYPOXEMIA", Math.max(t, o.getTimestamp())));
                    return;
                }
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

