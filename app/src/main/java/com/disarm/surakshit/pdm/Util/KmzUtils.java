package com.disarm.surakshit.pdm.Util;

import android.content.Context;
import android.os.Environment;

import com.snatik.storage.Storage;

import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by naman on 24/12/17.
 */

public class KmzUtils {
    Context context;
    public KmzUtils(Context context){
        this.context = context;
    }

    //Extract kml file from a kmz file and copy it to Show directory
    public boolean copyKMLfromKMZToShow(File kmzFile){
        try {
            File tmpFolder = GetFolders.getTmpKMZExtractForCopyToShow();
            UnZip unZip = new UnZip(tmpFolder.getPath() + "/", kmzFile.toString());
            String absoluteFileName = getAbsoluteFileName(kmzFile.getName());
            int versionNo = getLatestDiffVersionNo(kmzFile.getName());
            String source = tmpFolder.getPath() + "/index.kml";
            String destination = "DMS/Show/" + absoluteFileName + versionNo + ".kml";
            Storage storage = new Storage(context);
            storage.move(source, destination);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    public static String getTimeStamp(){
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    private String getAbsoluteFileName(String fileName){
        Pattern pattern = Pattern.compile("_");
        String[] result = pattern.split(fileName);
        String absoluteFileName = "";
        for(int i=0;i<9;i++){
            absoluteFileName = absoluteFileName+result[i]+"_";
        }
        return absoluteFileName;
    }

    private int getLatestDiffVersionNo(String fileName){
        int version_number = 0;
        File working = GetFolders.getWorkingDir();
        File[] workingFiles = working.listFiles();
        String absoluteFileName = getAbsoluteFileName(fileName);
        for(int i=0;i<workingFiles.length;i++){
            if(workingFiles[i].getName().contains(absoluteFileName) && !workingFiles[i].getName().contains("diff")){
                String oldDiffName = workingFiles[i].getName();
                Pattern pattern = Pattern.compile("_");
                String[] result = pattern.split(oldDiffName);
                version_number = Integer.parseInt(result[9]);
            }
        }
        return version_number;
    }

}
