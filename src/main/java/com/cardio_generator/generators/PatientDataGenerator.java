package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;
/**
 * Defines a common method for classes that generate simulated patient data.
 * <p>Implementations of this interface produce a specific type of health data
 * and send the generated result to the selected output strategy.
 */
public interface PatientDataGenerator {
    /**
     * Generates data for a specific patient and sends it to an output strategy.
     * @param patientId: the unique identifier of the patient for whom data is generated
     * @param outputStrategy: the strategy used to output the generated data
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}