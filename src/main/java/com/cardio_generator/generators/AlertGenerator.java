package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;
/**
 * Generates simulated alert events for patients.
 * <p>This class keeps track of whether each patient currently has an active
 * alert. During each generation cycle, an active alert may be resolved, and
 * an inactive alert may be triggered based on probability.
 */
public class AlertGenerator implements PatientDataGenerator {
    // Changed field name from randomGenerator since static final mutable objects are not constants, should use lowerCamelCase, not constant-style naming
    // AlertStates to alertStates to match consistency
    public static final Random random = new Random();
    private boolean[] alertStates; // false = resolved, true = pressed
    /**
     * Creates an alert generator for a given number of patients.
     * @param patientCount the number of patients whose alert states are tracked
     */
    // Changed field name from AlertStates because non-constant fields use lowerCamelCase
    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }
    /**
     * Generates alert data for a patient and sends it to the selected output strategy.
     * <p>If the patient already has an active alert, there is a high probability
     * that the alert will be resolved. If the patient does not have an active
     * alert, a new alert may be triggered based on the configured rate.
     * @param patientId the unique identifier of the patient for whom data is generated
     * @param outputStrategy the strategy used to output the generated data
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            //changes to match variable names (AlertStates, randomGenerator)
            if (alertStates[patientId]) {
                if (random.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // remuved comment because of redundancy, method call already shows output
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                // Local vaiables use lowerCamelCase Lambda to lambda
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                //changes to match variable names (Lambda, randomGenerator)
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = random.nextDouble() < p;

                if (alertTriggered) {
                    //changes to match variable names (AlertStates)
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}