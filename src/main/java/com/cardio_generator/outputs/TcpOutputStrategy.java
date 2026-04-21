package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
/**
 * Outputs generated patient data to a connected TCP client.
 * <p>This class starts a TCP server on a given port, waits for a client
 * connection, and sends generated patient data as comma-separated text.
 */
public class TcpOutputStrategy implements OutputStrategy {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    /**
     * Creates a TCP output strategy and starts a server on the given port.
     * <p>A background thread is used to wait for a client connection so that the main thread is not blocked.
     * @param port the port on which the TCP server listens for a client connection
     */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Sends one generated patient data entry to the connected TCP client.
     * <p>The output format is a string containing the patient ID, timestamp, label, and data value.
     * If no client is connected, no data is sent.
     * @param patientId: the unique identifier of the patient
     * @param timestamp: the time at which the data was generated, in milliseconds since the Unix epoch
     * @param label: the type of data being output, like Alert or Saturation
     * @param data: the generated value or message to output
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}