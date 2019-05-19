package com.disarm.surakshit.pdm.P2PConnect;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logger {
    private String fileName;
    private static final String directoryName = "DMS/";
    private static final String LOGGER_TAG = "Logger";
    private String phoneNumber;


    public Logger(String phoneNumber, String fileName) {
        this.phoneNumber = phoneNumber;
        this.fileName = fileName;
        Log.v(LOGGER_TAG, "Logging Initiated");
    }

    public void addMessageToLog(String message) {
        File dir = Environment.getExternalStoragePublicDirectory(directoryName);
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = df.format(Calendar.getInstance().getTime());
        File logFile = new File(dir, fileName + "_" + phoneNumber + ".txt");
        if (!logFile.exists()) {
            try {
                Log.d(LOGGER_TAG, "File created ");
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileWriter buf = new FileWriter(logFile, true);
            buf.write(date + ',' + message + "\r\n");
            buf.flush();
            buf.close();
        } catch (Exception ignored) {
        }
    }

}
