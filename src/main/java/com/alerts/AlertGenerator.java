package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private DataStorage dataStorage;
    private final List<Alert> triggeredAlerts = new ArrayList<>();
    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert}
     * method. This method should define the specific conditions under which an
     * alert
     * will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        if (patient == null) {
            return;
        }

        List<PatientRecord> all = new ArrayList<>(patient.getRecords(Long.MIN_VALUE, Long.MAX_VALUE));
        if (all.isEmpty()) {
            return;
        }
        all.sort(Comparator.comparingLong(PatientRecord::getTimestamp));

        evaluateBloodPressure(all);
        evaluateSaturation(all);
        evaluateHypotensiveHypoxemia(all);
        evaluateEcg(all);
        evaluateManualAlert(all);
    }

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        if (alert == null) {
            return;
        }
        triggeredAlerts.add(alert);
        System.out.println("ALERT: patient=" + alert.getPatientId() + " condition=" + alert.getCondition() + " ts="
                + alert.getTimestamp());
    }
    /**
     * Returns alerts that were triggered during evaluation.
     */
    public List<Alert> getTriggeredAlerts() {
        return new ArrayList<>(triggeredAlerts);
    }

    private void evaluateBloodPressure(List<PatientRecord> all) {
        List<PatientRecord> sys = filterByType(all, "SystolicPressure");
        List<PatientRecord> dia = filterByType(all, "DiastolicPressure");

        // Critical thresholds
        for (PatientRecord r : sys) {
            if (r.getMeasurementValue() > 180.0 || r.getMeasurementValue() < 90.0) {
                triggerAlert(new Alert(Integer.toString(r.getPatientId()), "CRITICAL_SYSTOLIC_PRESSURE", r.getTimestamp()));
            }
        }
        for (PatientRecord r : dia) {
            if (r.getMeasurementValue() > 120.0 || r.getMeasurementValue() < 60.0) {
                triggerAlert(new Alert(Integer.toString(r.getPatientId()), "CRITICAL_DIASTOLIC_PRESSURE", r.getTimestamp()));
            }
        }

        // Trend: 3 consecutive readings, each step changes by >10 in same direction
        if (hasTrend(sys, true)) {
            PatientRecord last = sys.get(sys.size() - 1);
            triggerAlert(new Alert(Integer.toString(last.getPatientId()), "SYSTOLIC_TREND_ALERT", last.getTimestamp()));
        }
        if (hasTrend(dia, false)) {
            PatientRecord last = dia.get(dia.size() - 1);
            triggerAlert(new Alert(Integer.toString(last.getPatientId()), "DIASTOLIC_TREND_ALERT", last.getTimestamp()));
        }
    }

    private void evaluateSaturation(List<PatientRecord> all) {
        List<PatientRecord> sat = filterByType(all, "Saturation");
        if (sat.isEmpty()) {
            return;
        }

        for (PatientRecord r : sat) {
            if (r.getMeasurementValue() < 92.0) {
                triggerAlert(new Alert(Integer.toString(r.getPatientId()), "LOW_SATURATION", r.getTimestamp()));
            }
        }

        // Rapid drop: >=5 within 10 minutes.
        // "within a 10-minute interval" assumed to mean that the two readings can be anywhere inside a sliding
        // 10-minute window, not necessarily adjacent samples.
        final long tenMinutesMs = 10L * 60L * 1000L;
        int i = 0;
        for (int j = 0; j < sat.size(); j++) {
            while (sat.get(j).getTimestamp() - sat.get(i).getTimestamp() > tenMinutesMs) {
                i++;
            }
            for (int k = i; k < j; k++) {
                double drop = sat.get(k).getMeasurementValue() - sat.get(j).getMeasurementValue();
                if (drop >= 5.0) {
                    triggerAlert(new Alert(Integer.toString(sat.get(j).getPatientId()), "RAPID_SATURATION_DROP",
                            sat.get(j).getTimestamp()));
                    return;
                }
            }
        }
    }

    private void evaluateHypotensiveHypoxemia(List<PatientRecord> all) {
        List<PatientRecord> sys = filterByType(all, "SystolicPressure");
        List<PatientRecord> sat = filterByType(all, "Saturation");
        if (sys.isEmpty() || sat.isEmpty()) {
            return;
        }

        // Assuming "trigger when both occur" needs a time correlation rule
        // treat both as occurring within a +/-10 minute window (same window as the saturation rapid-drop rule), since the spec doesn't define stricter requirements
        final long tenMinutesMs = 10L * 60L * 1000L;
        for (PatientRecord s : sys) {
            if (s.getMeasurementValue() >= 90.0) {
                continue;
            }
            long t = s.getTimestamp();
            for (PatientRecord o : sat) {
                if (Math.abs(o.getTimestamp() - t) <= tenMinutesMs && o.getMeasurementValue() < 92.0) {
                    triggerAlert(new Alert(Integer.toString(s.getPatientId()), "HYPOTENSIVE_HYPOXEMIA", Math.max(t, o.getTimestamp())));
                    return;
                }
            }
        }
    }

    private void evaluateEcg(List<PatientRecord> all) {
        List<PatientRecord> ecg = filterByType(all, "ECG");
        if (ecg.size() < 10) {
            return;
        }

        // The spec says "sliding window average" and "peaks far beyond the current average" but doesn't define a numeric threshold.
        // So I compute mean/std over a sliding window and trigger when a point deviates by >3.5 standard devs
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

            // "far beyond the current average": use a conservative z-score style threshold
            if (std > 0.0001 && Math.abs(v - mean) > 3.5 * std) {
                triggerAlert(new Alert(Integer.toString(r.getPatientId()), "ABNORMAL_ECG_PEAK", r.getTimestamp()));
                return;
            }
        }
    }

    private void evaluateManualAlert(List<PatientRecord> all) {
        List<PatientRecord> manual = filterByType(all, "Alert");
        for (PatientRecord r : manual) {
            if (r.getMeasurementValue() >= 1.0) {
                // I assume that manual alerts are represented in storage as numeric state (1.0=triggered, 0.0=resolved),
                // produced by FileDataReader when reading the simulators Alert output lines
                triggerAlert(new Alert(Integer.toString(r.getPatientId()), "MANUAL_ALERT_TRIGGERED", r.getTimestamp()));
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

    private static boolean hasTrend(List<PatientRecord> records, boolean systolic) {
        if (records.size() < 3) {
            return false;
        }
        for (int i = 0; i + 2 < records.size(); i++) {
            double a = records.get(i).getMeasurementValue();
            double b = records.get(i + 1).getMeasurementValue();
            double c = records.get(i + 2).getMeasurementValue();

            double d1 = b - a;
            double d2 = c - b;
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
