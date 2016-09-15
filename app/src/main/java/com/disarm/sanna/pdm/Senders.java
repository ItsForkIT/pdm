package com.disarm.sanna.pdm;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by arka on 15/9/16.
 */
public class Senders {
    String number;
    ArrayList<File> allFiles;
    ArrayList<File> textFiles;
    ArrayList<File> imageFiles;
    ArrayList<File> videoFiles;
    ArrayList<File> recordingFiles;
    ArrayList<File> smsFiles;

    public Senders(String number) {
        this.number = number;
        allFiles = new ArrayList<>();
        textFiles = new ArrayList<>();
        imageFiles = new ArrayList<>();
        videoFiles = new ArrayList<>();
        recordingFiles = new ArrayList<>();
        smsFiles = new ArrayList<>();
    }

    public void addFile(File file) {
        this.allFiles.add(file);
    }

    public void addText(File file) {
        this.textFiles.add(file);
    }

    public void addImage(File file) {
        this.imageFiles.add(file);
    }

    public void addVideo(File file) {
        this.videoFiles.add(file);
    }

    public void addRecording(File file) {
        this.recordingFiles.add(file);
    }

    public void addSms(File file) {
        this.smsFiles.add(file);
    }
}