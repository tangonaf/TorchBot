package com.mt.nl.torchbot.services;

import com.fazecast.jSerialComm.SerialPort;
import com.sun.deploy.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ArduinoListenerMessagerTest {

    private static SerialPort port = null;
    private boolean listening = true;

    public static void main(String[] args) {
        try {

            run();

        } catch (Exception e) {
            System.out.println("printing out exception: " + e);

        }
    }

    private static void run() throws InterruptedException {
        SerialPort[] ports = SerialPort.getCommPorts();

        try {
            port = openConnection(ports, port);
            System.out.println("port is " + port);

        } catch (Exception ex) {
            System.out.println("Exception thrown while opening connection in constructor: " + ex);
        }

        InputStream inputStream = port.getInputStream();
        OutputStream outputStream = port.getOutputStream();

        ArduinoListenerMessagerTest a = new ArduinoListenerMessagerTest();
        ArduinoListenerMessagerTest b = new ArduinoListenerMessagerTest();

        new Thread(() -> a.arduinoListener(inputStream)).start();
        Thread.sleep(1000);
        new Thread(() -> b.arduinoMessager(outputStream)).start();
    }

    private void arduinoMessager(OutputStream outputStream) {

        System.out.println("Messaging to arduino");
        outputStream = port.getOutputStream();

        try {
            File file = new File("D:\\Overall_Projects\\TorchBot\\test.txt");
            Path yourPath = file.toPath();
            byte[] encoded = Files.readAllBytes(yourPath);
            String textFile = new String(encoded, StandardCharsets.UTF_8);
            List<String> commaSeparatedList = Arrays.asList(textFile.split(","));
            String eol = "\n";
            Thread.sleep(2000);
            for (String line : commaSeparatedList) {
                System.out.println("Printing out : " + line);
                outputStream.write(line.getBytes());
                outputStream.write(eol.getBytes());
            }

        } catch (Exception e) {
            System.out.println("printing out exception: " + e);
        }
    }

    private void arduinoListener(InputStream inputStream) {
        System.out.println("Listening to arduino");
        List<String> arduinoSequence = new ArrayList<>();
        while (listening) {
            try {
                Thread.sleep(1000);
                int length = inputStream.available();
                byte[] buffer = new byte[length];
                while (inputStream.available() > 0) {

                    int iBytes = inputStream.read(buffer);

                    if (iBytes > 0) {
                        System.out.printf("InputStream contains %d characters \n", iBytes);
                    }
                    String result = new String(buffer, StandardCharsets.UTF_8);

                    System.out.println("Reading from Arduino: \n " + result);
                    if (!result.matches("^[a-zA-Z<>\\s]+$")) {
                        System.out.println("Adding to arduinoSequence" + result);
                        arduinoSequence.add(result);
                    }
                    if (result.contains("-32000")) {
                        System.out.println("Array will be stored in output file");
                        String stringSeq = StringUtils.join(arduinoSequence, ",");
                        BufferedWriter writer = new BufferedWriter(new FileWriter("test.txt"));
                        writer.write(stringSeq);
                        writer.close();
                        System.out.println("Stopping with Listening");
                        listening = false;
                        inputStream.close();
                    }
                }
            } catch (Exception e) {
                System.out.println("printing out exception in Arduino Listener: " + e);
            }
        }
    }

    private static SerialPort openConnection(SerialPort[] ports, SerialPort port) {
        for (SerialPort p : ports) {
            System.out.println(p.getSystemPortName());
            if (p.getSystemPortName().equals("COM3")) {  /*for your question, <required_port> would be ttyACM0, but this can change if you reconnect the device, or if multiple devices are connected.*/
                port = p;
            }
        }

        if (port == null) {
            return null;
        }

        port.setComPortParameters(9600, 8, 1, 0); // default connection settings for Arduino
        port.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0); // block until bytes can be written

        if (port.openPort()) {
            System.out.println("Port is open :) and port is " + port.toString());
        } else {
            System.out.println("Failed to open port :(");
            return null;
        }

        return port;
    }
}
