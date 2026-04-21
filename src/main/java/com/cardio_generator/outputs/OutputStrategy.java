package com.cardio_generator.outputs;
/**
 * Defines a common method for classes that output generated patient data.
 * <p>Classes that implement this interface decide how the generated data is
 * delivered, such as writing it to the console, saving it to a file or sending it over a network connection.
 */
public interface OutputStrategy {
    /**
     * Outputs one generated data value for a patient.
     * @param patientId: the unique identifier of the patient
     * @param timestamp: the time at which the data was generated, in milliseconds since the Unix epoch
     * @param label: the type of data being output, like Alert or Saturation
     * @param data: the generated value or message to output
     */
    void output(int patientId, long timestamp, String label, String data);
}