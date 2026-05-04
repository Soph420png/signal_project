package alerts;

import static org.junit.jupiter.api.Assertions.*;

import com.alerts.Alert;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class AlertGeneratorTest {
    @BeforeEach
    void resetSingleton() {
        com.data_management.DataStorage.resetInstanceForTests();
    }

    @Test
    void triggersLowSaturationAlert() {
        DataStorage storage=DataStorage.getInstance();
        storage.addPatientData(1, 91.0, "Saturation", 1000L);

        Patient p = storage.getAllPatients().get(0);
        AlertGenerator gen = new AlertGenerator(storage);
        gen.evaluateData(p);

        List<Alert> alerts = gen.getTriggeredAlerts();
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().equals("LOW_SATURATION")));
    }

    @Test
    void triggersBloodPressureTrendAlert() {
        DataStorage storage=DataStorage.getInstance();
        storage.addPatientData(1, 100.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 112.0, "SystolicPressure", 2000L); // +12
        storage.addPatientData(1, 125.0, "SystolicPressure", 3000L); // +13

        Patient p = storage.getAllPatients().get(0);
        AlertGenerator gen = new AlertGenerator(storage);
        gen.evaluateData(p);

        List<Alert> alerts = gen.getTriggeredAlerts();
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().equals("SYSTOLIC_TREND_ALERT")));
    }

    @Test
    void triggersBloodPressureDecreasingTrendAlert() {
        DataStorage storage=DataStorage.getInstance();
        storage.addPatientData(1, 140.0, "DiastolicPressure", 1000L);
        storage.addPatientData(1, 128.0, "DiastolicPressure", 2000L); // -12
        storage.addPatientData(1, 115.0, "DiastolicPressure", 3000L); // -13

        Patient p = storage.getAllPatients().get(0);
        AlertGenerator gen = new AlertGenerator(storage);
        gen.evaluateData(p);

        assertTrue(gen.getTriggeredAlerts().stream().anyMatch(a -> a.getCondition().equals("DIASTOLIC_TREND_ALERT")));
    }

    @Test
    void triggersCriticalBloodPressureAlerts() {
        DataStorage storage=DataStorage.getInstance();
        storage.addPatientData(1, 181.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 59.0, "DiastolicPressure", 2000L);

        Patient p = storage.getAllPatients().get(0);
        AlertGenerator gen = new AlertGenerator(storage);
        gen.evaluateData(p);

        List<Alert> alerts = gen.getTriggeredAlerts();
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().equals("CRITICAL_SYSTOLIC_PRESSURE")));
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().equals("CRITICAL_DIASTOLIC_PRESSURE")));
    }

    @Test
    void triggersRapidSaturationDropAlertWithinTenMinutes() {
        DataStorage storage=DataStorage.getInstance();
        storage.addPatientData(1, 98.0, "Saturation", 0L);
        // assuming that "within a 10-minute interval" means a sliding 10-minute window.
        storage.addPatientData(1, 93.0, "Saturation", 9L * 60L * 1000L); // drop 5 within 10 minutes

        Patient p = storage.getAllPatients().get(0);
        AlertGenerator gen = new AlertGenerator(storage);
        gen.evaluateData(p);

        assertTrue(gen.getTriggeredAlerts().stream().anyMatch(a -> a.getCondition().equals("RAPID_SATURATION_DROP")));
    }

    @Test
    void triggersHypotensiveHypoxemiaAlert() {
        DataStorage storage=DataStorage.getInstance();
        storage.addPatientData(1, 89.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 91.0, "Saturation", 1500L);

        Patient p = storage.getAllPatients().get(0);
        AlertGenerator gen = new AlertGenerator(storage);
        gen.evaluateData(p);

        assertTrue(gen.getTriggeredAlerts().stream().anyMatch(a -> a.getCondition().equals("HYPOTENSIVE_HYPOXEMIA")));
    }

    @Test
    void triggersManualAlertTriggered() {
        DataStorage storage=DataStorage.getInstance();
        storage.addPatientData(1, 1.0, "Alert", 1000L); // from FileDataReader mapping

        Patient p = storage.getAllPatients().get(0);
        AlertGenerator gen = new AlertGenerator(storage);
        gen.evaluateData(p);

        assertTrue(gen.getTriggeredAlerts().stream().anyMatch(a -> a.getCondition().equals("MANUAL_ALERT_TRIGGERED")));
    }

    @Test
    void triggersAbnormalEcgPeakAlert() {
        DataStorage storage=DataStorage.getInstance();
        long t = 0L;
        // "far beyond average" is implemented using a mean/std sliding window threshold (indicating assumption)
        for (int i = 0; i < 40; i++) {
            storage.addPatientData(1, 0.10, "ECG", t);
            t += 1000L;
        }
        // single big spike
        storage.addPatientData(1, 5.0, "ECG", t);

        Patient p = storage.getAllPatients().get(0);
        AlertGenerator gen = new AlertGenerator(storage);
        gen.evaluateData(p);

        assertTrue(gen.getTriggeredAlerts().stream().anyMatch(a -> a.getCondition().equals("ABNORMAL_ECG_PEAK")));
    }
}

