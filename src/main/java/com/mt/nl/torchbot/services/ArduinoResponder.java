package com.mt.nl.torchbot.services;

import com.fazecast.jSerialComm.SerialPort;
import com.mt.nl.torchbot.domain.ArduinoListener;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.util.List;

@Slf4j
public class ArduinoResponder implements ArduinoListener {

    private FileService fileService = new FileService();

    private String eol = "\n";

    /**
     * When the java service receives a string from the arduino it sends it to the fileservice
     * The file will be saved under a name specified by the end-user
     *
     * @param array
     */
    public void importFile(String array) {
        fileService.gettingArrayFromArduino(array);
    }

    public void exportFile(SerialPort port) {
        log.info("Sending File!");
        log.info("Arduino expects to get an array; sending array to arduino");
        arduinoMessager(port);
    }

    /**
     * When the java service receives a signal from the arduino that it's waiting for a file
     *
     * @param port
     */
    private void arduinoMessager(SerialPort port) {

        log.info("Messaging to arduino");
        OutputStream outputStream = port.getOutputStream();

        try {
            List<String> commaSeparatedList = fileService.getExportedArray();

            if (commaSeparatedList != null) {

                Thread.sleep(100);
                for (String line : commaSeparatedList) {
                    if (line.matches("^-?[0-9]+$")) {
                        log.info("Printing out : " + line);
                        outputStream.write(line.getBytes());
                        outputStream.write(eol.getBytes());
                    }
                }
                outputStream.close();
            } else {
                String noArrayAvailable = "No_Array_Available" + eol;
                Thread.sleep(100);

                outputStream.write(noArrayAvailable.getBytes());
                outputStream.write(eol.getBytes());
            }
        } catch (
                Exception e) {
            log.error("Printing out exception: " + e);
        }
    }
}

