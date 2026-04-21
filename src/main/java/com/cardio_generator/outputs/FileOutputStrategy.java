package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Outputs generated patient data to text files.
 * <p>This class stores generated data in files inside a base directory.
 * Each data label is written to its own file
 * so different kinds of simulated patient data are separated by type.
 */
public class FileOutputStrategy implements OutputStrategy {
    //lowerCamelCase "BaseDirectory" to "baseDirectory"
    private String baseDirectory;

    //lowerCamelCase "file_map" to "fileMap"
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();
    /**
     * Creates a file output strategy that writes data to the given directory.
     * @param baseDirectory the directory where output files will be created
     */
    public FileOutputStrategy(String baseDirectory) {

        this.baseDirectory = baseDirectory;
    }
    /**
     * Writes one generated patient data entry to a file based on its label.
     * <p>If the base directory does not exist its created before writing.
     * Data is appended to a text file associated with the given label.
     * @param patientId: the unique identifier of the patient
     * @param timestamp: the time at which the data was generated, in milliseconds since the Unix epoch
     * @param label: the type of data being output, like Alert or Saturation
     * @param data: the generated value or message to output
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            // changed baseDirectory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        //lowerCamelCase "FilePath" to "filePath",
        // Set the filePath variable
        String filePath = fileMap.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}