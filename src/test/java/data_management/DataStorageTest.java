package com.data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.PatientRecord;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class DataStorageTest {

    @Test
    void testAddAndGetRecords() {
        // TODO Perhaps you can implement a mock data reader to mock the test data?
        // DataReader reader
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size()); // Check if two records are retrieved
        assertEquals(100.0, records.get(0).getMeasurementValue()); // Validate first record
    }

    @Test
    void getRecordsReturnsEmptyForUnknownPatient() {
        DataStorage storage = new DataStorage();
        assertTrue(storage.getRecords(999, 0L, Long.MAX_VALUE).isEmpty());
    }

    @Test
    void testFileDataReaderReadsDirectory() throws IOException {
        Path dir = Files.createTempDirectory("signal_output_");
        Path heartRateFile = dir.resolve("HeartRate.txt");
        Path alertFile = dir.resolve("Alert.txt");
        Path ignoreMe = dir.resolve("ignore.csv");

        Files.writeString(
                heartRateFile,
                "Patient ID: 1, Timestamp: 1714376789050, Label: HeartRate, Data: 72.5\n" +
                        "Patient ID: 1, Timestamp: 1714376789051, Label: HeartRate, Data: 73.0\n",
                StandardCharsets.UTF_8);

        // non-numeric data should be ignored by FileDataReader
        Files.writeString(
                alertFile,
                "Patient ID: 1, Timestamp: 1714376789052, Label: Alert, Data: HIGH_BP\n",
                StandardCharsets.UTF_8);

        Files.writeString(
                ignoreMe,
                "Patient ID: 1, Timestamp: 1714376789053, Label: HeartRate, Data: 999\n",
                StandardCharsets.UTF_8);

        DataStorage storage = new DataStorage();
        new FileDataReader(dir).readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(2, records.size());
        assertEquals("HeartRate", records.get(0).getRecordType());
        assertEquals(72.5, records.get(0).getMeasurementValue());
    }

    @Test
    void testFileDataReaderStoresManualAlertTriggeredResolved() throws IOException {
        Path dir = Files.createTempDirectory("signal_output_alert_");
        Path alertFile = dir.resolve("Alert.txt");
        Files.writeString(
                alertFile,
                "Patient ID: 1, Timestamp: 1000, Label: Alert, Data: triggered\n" +
                        "Patient ID: 1, Timestamp: 2000, Label: Alert, Data: resolved\n",
                StandardCharsets.UTF_8);

        DataStorage storage = new DataStorage();
        new FileDataReader(dir).readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(2, records.size());
        assertEquals("Alert", records.get(0).getRecordType());
        assertEquals(1.0, records.get(0).getMeasurementValue());
        assertEquals(0.0, records.get(1).getMeasurementValue());
    }

    @Test
    void testFileDataReaderSkipsMalformedLines() throws IOException {
        Path dir = Files.createTempDirectory("signal_output_malformed_");
        Path file = dir.resolve("HeartRate.txt");
        Files.writeString(
                file,
                "not a valid line\n" +
                        "Patient ID: 1, Timestamp: 1000, Label: HeartRate, Data: 60\n",
                StandardCharsets.UTF_8);

        DataStorage storage = new DataStorage();
        new FileDataReader(dir).readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(60.0, records.get(0).getMeasurementValue());
    }
}
