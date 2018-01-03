package com.disarm.surakshit.pdm.Util;

import android.content.Context;

import com.snatik.storage.Storage;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Created by naman on 24/12/17.
 */

public class DiffUtils {
    Context context;
    public DiffUtils(Context context){
        this.context = context;
    }


    //Source file KMZ format
    //Destination file KML format
    private void createDiff(File source,File destination){
        Storage storage = new Storage(context);
        File tmpKMZExtractDir = GetFolders.getTmpKMZExtractForKMLDir();
        UnZip unZip = new UnZip(tmpKMZExtractDir.getPath()+"/",source.toString());
    }



    private String getDeltaName(String fileName){
        String absolutueFileName = getAbsoluteFileName(fileName);
        int old_version = getLatestDiffVersionNo(fileName);
        int new_version = old_version++;
        String timestamp = KmzUtils.getTimeStamp();
        return absolutueFileName+timestamp+"_"+new_version+".diff";
    }

    private int getLatestDiffVersionNo(String fileName){
        int version_number = 0;
        File working = GetFolders.getWorkingDir();
        File[] workingFiles = working.listFiles();
        String absoluteFileName = getAbsoluteFileName(fileName);
        for(int i=0;i<workingFiles.length;i++){
            if(workingFiles[i].getName().contains(absoluteFileName) && !workingFiles[i].getName().contains("kmz")){
                String oldDiffName = workingFiles[i].getName();
                Pattern pattern = Pattern.compile("_");
                String[] result = pattern.split(oldDiffName);
                version_number = Integer.parseInt(result[10]);
            }
        }
        return version_number;
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

}
