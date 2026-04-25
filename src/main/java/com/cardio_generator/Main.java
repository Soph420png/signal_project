package com.cardio_generator;

import com.data_management.DataStorage;

/**
 * Can run either the simulator or the storage demo.
 * (default) runs HealthDataSimulator
 *  args[0] == "DataStorage" runs DataStorage
 */
public final class Main {
    private Main() {
    }
    public static void main(String[] args) {
        if (args != null && args.length > 0 && "DataStorage".equalsIgnoreCase(args[0])) {
            DataStorage.main(sliceArgs(args, 1));
            return;
        }
        try {
            HealthDataSimulator.main(args == null ? new String[0] : args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to run HealthDataSimulator", e);
        }
    }

    private static String[] sliceArgs(String[] args, int startIdx) {
        if (args == null || startIdx >= args.length) {
            return new String[0];
        }
        String[] out = new String[args.length - startIdx];
        System.arraycopy(args, startIdx, out, 0, out.length);
        return out;
    }
}
