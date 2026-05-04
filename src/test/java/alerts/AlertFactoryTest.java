package alerts;

import static org.junit.jupiter.api.Assertions.*;

import com.alerts.Alert;
import com.alerts.AlertFactory;
import com.alerts.BloodOxygenAlert;
import com.alerts.BloodOxygenAlertFactory;
import com.alerts.BloodPressureAlert;
import com.alerts.BloodPressureAlertFactory;
import com.alerts.ECGAlertFactory;
import com.alerts.EcgAlert;
import org.junit.jupiter.api.Test;

class AlertFactoryTest {

    @Test
    void bloodPressureFactoryCreatesBloodPressureAlert() {
        BloodPressureAlertFactory factory=new BloodPressureAlertFactory();
        Alert alert = factory.createAlert("7", "CRITICAL_SYSTOLIC_PRESSURE", 100L);
        assertTrue(alert instanceof BloodPressureAlert);
        assertEquals("7", alert.getPatientId());
        assertEquals("CRITICAL_SYSTOLIC_PRESSURE", alert.getCondition());
        assertEquals(100L, alert.getTimestamp());
    }

    @Test
    void bloodOxygenFactoryCreatesBloodOxygenAlert() {
        BloodOxygenAlertFactory factory=new BloodOxygenAlertFactory();
        Alert alert = factory.createAlert("2", "LOW_SATURATION", 200L);
        assertTrue(alert instanceof BloodOxygenAlert);
        assertEquals("LOW_SATURATION", alert.getCondition());
    }

    @Test
    void ecgFactoryCreatesEcgAlert() {
        ECGAlertFactory factory=new ECGAlertFactory();
        Alert alert = factory.createAlert("3", "ABNORMAL_ECG_PEAK", 300L);
        assertTrue(alert instanceof EcgAlert);
    }

    @Test
    void createDispatchesToCorrectConcreteType() {
        assertTrue(AlertFactory.create("1", "SYSTOLIC_TREND_ALERT", 0L) instanceof BloodPressureAlert);
        assertTrue(AlertFactory.create("1", "RAPID_SATURATION_DROP", 0L) instanceof BloodOxygenAlert);
        assertTrue(AlertFactory.create("1", "ABNORMAL_ECG_PEAK", 0L) instanceof EcgAlert);
    }

    @Test
    void createRejectsUnknownCondition() {
        assertThrows(IllegalArgumentException.class,
                () -> AlertFactory.create("1", "UNKNOWN_CODE", 0L));
    }
}
