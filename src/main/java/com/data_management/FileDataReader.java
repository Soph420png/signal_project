package com.data_management;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads data from a directory written by {@code --output file:<directory>}.
 * <p>
 * The generator writes one file per label, with lines in the format:
 * {@code Patient ID: <id>, Timestamp: <ts>, Label: <label>, Data: <data>}.
 * <p>
 * This reader loads all {@code *.txt} files in the directory and stores numeric
 * {@code Data} values into {@link DataStorage}.
 */
public class FileDataReader implements DataReader {
    private static final Pattern LINE_PATTERN = Pattern.compile(
            "^Patient ID:\\s*(\\d+),\\s*Timestamp:\\s*(\\d+),\\s*Label:\\s*([^,]+),\\s*Data:\\s*(.*)$");
    private final Path outputDirectory;

    public FileDataReader(String outputDirectory) {
        this(Paths.get(Objects.requireNonNull(outputDirectory, "outputDirectory")));
    }

    public FileDataReader(Path outputDirectory) {
        this.outputDirectory = Objects.requireNonNull(outputDirectory, "outputDirectory");
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        Objects.requireNonNull(dataStorage, "dataStorage");

        if (!Files.exists(outputDirectory)) {
            throw new IOException("Output directory does not exist: " + outputDirectory);
        }
        if (!Files.isDirectory(outputDirectory)) {
            throw new IOException("Output path is not a directory: " + outputDirectory);
        }

        try (DirectoryStream<Path> files = Files.newDirectoryStream(outputDirectory, "*.txt")) {
            for (Path file : files) {
                readFileIntoStorage(file, dataStorage);
            }
        }
    }

    private static void readFileIntoStorage(Path file, DataStorage dataStorage) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                ParsedEntry entry = parseLine(line);
                if (entry == null) {
                    continue;
                }
                dataStorage.addPatientData(entry.patientId, entry.measurementValue, entry.recordType, entry.timestamp);
            }
        }
    }

    private static ParsedEntry parseLine(String line) {
        Matcher matcher = LINE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return null;
        }

        int patientId;
        long timestamp;
        String label;
        String data;
        try {
            patientId = Integer.parseInt(matcher.group(1));
            timestamp = Long.parseLong(matcher.group(2));
            label = matcher.group(3).trim();
            data = matcher.group(4).trim();
        } catch (RuntimeException e) {
            return null;
        }

        Double measurementValue = tryParseDouble(data);
        if (measurementValue == null) {
            // manual alert button events are stored as numeric values for downstream evaluation
            // triggered => 1.0 and resolved => 0.0
            // assumtion is that since DataStorage currently stores only numeric measurements, we encode
            // the simulators "Alert" events as numeric state changes rather than adding a new
            // non-numeric record type. keeps storage simple and alert button testable with the PatientRecord.
            if ("Alert".equalsIgnoreCase(label)) {
                if ("triggered".equalsIgnoreCase(data)) {
                    return new ParsedEntry(patientId, timestamp, "Alert", 1.0);
                }
                if ("resolved".equalsIgnoreCase(data)) {
                    return new ParsedEntry(patientId, timestamp, "Alert", 0.0);
                }
            }
            return null;
        }

        return new ParsedEntry(patientId, timestamp, label, measurementValue);
    }

    private static Double tryParseDouble(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static final class ParsedEntry {
        final int patientId;
        final long timestamp;
        final String recordType;
        final double measurementValue;

        ParsedEntry(int patientId, long timestamp, String recordType, double measurementValue) {
            this.patientId = patientId;
            this.timestamp = timestamp;
            this.recordType = recordType;
            this.measurementValue = measurementValue;
        }
    }
}