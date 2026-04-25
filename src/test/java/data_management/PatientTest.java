package data_management;

import static org.junit.jupiter.api.Assertions.*;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

class PatientTest {

    @Test
    void getRecordsFiltersInclusiveRange() {
        Patient p = new Patient(1);
        p.addRecord(10.0, "HeartRate", 1000L);
        p.addRecord(11.0, "HeartRate", 2000L);
        p.addRecord(12.0, "HeartRate", 3000L);

        List<PatientRecord> records = p.getRecords(2000L, 3000L);
        assertEquals(2, records.size());
        assertEquals(2000L, records.get(0).getTimestamp());
        assertEquals(3000L, records.get(1).getTimestamp());
    }

    @Test
    void getRecordsReturnsEmptyWhenStartAfterEnd() {
        Patient p = new Patient(1);
        p.addRecord(10.0, "HeartRate", 1000L);

        assertTrue(p.getRecords(2000L, 1000L).isEmpty());
    }
}

