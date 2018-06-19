package com.disarm.surakshit.pdm.BackgroundProcess;

import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.disarm.surakshit.pdm.Database.App;
import com.disarm.surakshit.pdm.Database.SavedFileName;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.snatik.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

/**
 * Created by AmanKumar on 6/19/2018.
 */

public class UploadJobService extends JobService {
    private StorageReference mStorageRef;
    public static final String FILES_CONST = "Kml_Files";

    @Override
    public boolean onStartJob(JobParameters job) {
        //fireBase changes
        mStorageRef = FirebaseStorage.getInstance().getReference();
        Log.d("Upload Job", "Im here");
        new Thread(new Runnable() {
            @Override
            public void run() {
                uploadFiles();
            }
        }).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

    private void uploadFiles() {
        File kmlDir = Environment.getExternalStoragePublicDirectory("DMS/tmpKML/");
        List<String> filesInDb = getFilesInDb();
        if (kmlDir.listFiles().length > 0) {
            //there are files in tempKMZ directory that need to be uploaded
            for (File file : kmlDir.listFiles()) {
                if (!filesInDb.contains(file.getName())) {
                    //files not uploaded
                    Log.d("Not Uploaded", "file name" + file.getName());
                    saveToFirebase(file.getName());
                }
            }
        }
    }

    private void saveToFirebase(final String file_name) {
        File fileToUpload = Environment.getExternalStoragePublicDirectory("DMS/tmpKML/" + file_name);
        StorageReference fileRef = mStorageRef.child(FILES_CONST).child(fileToUpload.getName());
        if (fileToUpload.exists()) {
            final Box<SavedFileName> savedFiles = ((App) getApplication()).getBoxStore().boxFor(SavedFileName.class);
            final SavedFileName savedFileName = new SavedFileName(file_name);
            Uri fileUri = Uri.fromFile(fileToUpload);
            UploadTask uploadTask = fileRef.putFile(fileUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d("Upload Test", "Upload Successful");
                    //save file_name in database
                    savedFiles.put(savedFileName);
                    //delete saved file from tmpKML
                    File tempFile = Environment.getExternalStoragePublicDirectory("DMS/tmpKML/" + file_name);
                    Storage storage = new Storage(getApplicationContext());
                    storage.deleteFile(tempFile.getAbsolutePath());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Upload Test", "Upload Failed:" + e.getMessage());
                }
            });
            savedFiles.closeThreadResources();
        } else
            Log.d("Upload Test", "No File to upload");
    }

    private List<String> getFilesInDb() {
        Box<SavedFileName> savedFiles = ((App) getApplication()).getBoxStore().boxFor(SavedFileName.class);
        List<SavedFileName> files = savedFiles.getAll();
        List<String> fileNames = new ArrayList<>(files.size());
        for (SavedFileName file : files) {
            fileNames.add(file.getFileName());
        }
        savedFiles.closeThreadResources();
        return fileNames;
    }
}
