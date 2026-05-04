package alerts;

import static org.junit.jupiter.api.Assertions.*;

import com.alerts.Alert;
import com.alerts.BasicAlert;
import com.alerts.PriorityAlertDecorator;
import com.alerts.RepeatedAlertDecorator;
import org.junit.jupiter.api.Test;

class AlertDecoratorTest {

    @Test
    void priorityDecoratorPreservesAlertFields() {
        Alert base=new BasicAlert("1", "LOW_SATURATION", 1000L);
        PriorityAlertDecorator decorated = new PriorityAlertDecorator(base, 10);
        assertEquals("1", decorated.getPatientId());
        assertEquals("LOW_SATURATION", decorated.getCondition());
        assertEquals(1000L, decorated.getTimestamp());
        assertEquals(10, decorated.getPriority());
    }

    @Test
    void repeatedDecoratorPreservesAlertFields() {
        Alert base=new BasicAlert("2", "RAPID_SATURATION_DROP", 2000L);
        RepeatedAlertDecorator decorated = new RepeatedAlertDecorator(base, 60_000L);
        assertEquals("2", decorated.getPatientId());
        assertEquals("RAPID_SATURATION_DROP", decorated.getCondition());
        assertEquals(2000L, decorated.getTimestamp());
        assertEquals(60_000L, decorated.getRepeatIntervalMs());
    }
}

