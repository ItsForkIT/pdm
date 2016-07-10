package com.disarm.sanna.pdm.Background;


import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;


/**
 * Created by Sanna on 05-07-2016.
 */
public class FileTask extends AsyncTask  {
    String fileType,groupType,timestamp,groupID,ttl,dest,source;
    String[] fileName;

    @Override
    protected synchronized void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Object doInBackground(Object[] objects) {
        ttl = (String) objects[0];
        dest = (String) objects[1];
        source = "9888844036";
        File logFile = null;

       String state = Environment.getExternalStorageState();
        File dir = Environment.getExternalStoragePublicDirectory("DMS/Working");
        Log.v("Logger Initiated","");
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (!dir.exists()) {
                Log.d("Dir created ", "Dir created ");
                dir.mkdirs();
            }
            File[] foundFiles = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {

                    return name.startsWith("MapDisarm_Log_");
                }
            });

            Log.v("Found Files starting with MapDisarm_Log:", foundFiles.length + "");

            if (foundFiles != null && foundFiles.length > 0) {
                logFile = new File(foundFiles[0].toString());
                Log.v("LogFile:", foundFiles[0].toString());
            }

        }
        String[] latlongline = ReadLastLine.tail(logFile).split(",");
        String latlng = latlongline[0]+"_"+latlongline[1];

        String pathFrom = Environment.getExternalStorageDirectory().toString()+"/DMS/tmp";
        String pathTo = Environment.getExternalStorageDirectory().toString()+"/DMS/Working";
        Log.d("Files", "Path: " + pathFrom);
        File f = new File(pathFrom);
        File file[] = f.listFiles();
        Log.d("Files", "Size: "+ file.length);
        for (int i=0; i < file.length; i++)
        {
            fileName = file[i].getName().split("_");
            fileType = fileName[0];
            groupType = fileName[1];
            timestamp = fileName[2];
            groupID = fileName[3];
            Log.v("FIleNames",fileType+ttl+groupType+source+dest+latlng+timestamp+groupID);
            File from = new File(pathFrom,file[i].getName());
            String acutalFileName = fileType+"_"+ttl+"_"+groupType+"_"+source+"_"+dest+"_"+latlng+"_"+timestamp+"_"+groupID;
            File to = new File(pathTo,acutalFileName);
            from.renameTo(to);

        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }
}
