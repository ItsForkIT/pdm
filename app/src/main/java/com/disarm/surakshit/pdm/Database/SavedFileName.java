package com.disarm.surakshit.pdm.Database;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by AmanKumar on 6/18/2018.
 */
@Entity
public class SavedFileName {
    @Id
    long id;
    String fileName;

    public SavedFileName() {
    }

    public SavedFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
