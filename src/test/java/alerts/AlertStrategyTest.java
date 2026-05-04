package alerts;

import static org.junit.jupiter.api.Assertions.*;

import com.alerts.Alert;
import com.alerts.BloodPressureStrategy;
import com.alerts.HeartRateStrategy;
import com.alerts.OxygenSaturationStrategy;
import com.data_management.PatientRecord;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

class AlertStrategyTest {

    @Test
    void bloodPressureStrategyTriggersCriticalSystolic() {
        List<PatientRecord> records= List.of(
                new PatientRecord(1, 181.0, "SystolicPressure", 1000L)
        );
        List<Alert> alerts = run(records, new BloodPressureStrategy());
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().equals("CRITICAL_SYSTOLIC_PRESSURE")));
    }

    @Test
    void oxygenSaturationStrategyTriggersRapidDrop() {
        List<PatientRecord> records= List.of(
                new PatientRecord(1, 98.0, "Saturation", 0L),
                new PatientRecord(1, 93.0, "Saturation", 9L * 60L * 1000L)
        );
        List<Alert> alerts = run(records, new OxygenSaturationStrategy());
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().equals("RAPID_SATURATION_DROP")));
    }

    @Test
    void heartRateStrategyTriggersAbnormalEcgPeak() {
        List<PatientRecord> records = new ArrayList<>();
        long t = 0L;
        for (int i = 0; i < 40; i++) {
            records.add(new PatientRecord(1, 0.10, "ECG", t));
            t += 1000L;
        }
        records.add(new PatientRecord(1, 5.0, "ECG", t));

        List<Alert> alerts = run(records, new HeartRateStrategy());
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().equals("ABNORMAL_ECG_PEAK")));
    }

    private static List<Alert> run(List<PatientRecord> records, com.alerts.AlertStrategy strategy) {
        List<PatientRecord> sorted= new ArrayList<>(records);
        sorted.sort(Comparator.comparingLong(PatientRecord::getTimestamp));
        List<Alert> out= new ArrayList<>();
        strategy.checkAlert(sorted, out::add);
        return out;
    }
}

