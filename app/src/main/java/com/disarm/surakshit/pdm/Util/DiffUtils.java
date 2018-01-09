package com.disarm.surakshit.pdm.Util;

import android.content.Context;
import android.os.Environment;

import com.snatik.storage.Storage;

import java.io.File;
import java.util.regex.Pattern;

import ie.wombat.jbdiff.JBDiff;
import ie.wombat.jbdiff.JBPatch;

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
        File tmpKMZExtractDir = GetFolders.getTmpKMZExtractForKMLDir();
        UnZip unZip = new UnZip(tmpKMZExtractDir.getPath()+"/",source.toString());
        File sourceKML = Environment.getExternalStoragePublicDirectory(tmpKMZExtractDir.getPath()+"/index.kml");
        File delta = Environment.getExternalStoragePublicDirectory("/DMS/Working/"+getDeltaName(source.getName()));
        try {
            JBDiff.bsdiff(sourceKML, destination, delta);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //Source file KMZ format
    private void applyPatch(File source){
        File tmpKMZExtractDir = GetFolders.getTmpKMZExtractForKMLDir();
        UnZip unZip = new UnZip(tmpKMZExtractDir.getPath()+"/",source.toString());
        File sourceKML = Environment.getExternalStoragePublicDirectory(tmpKMZExtractDir.getPath()+"/index.kml");
        File delta = getLatestDeltaFile(source.getName());
        if(delta!=null){
            File destination = getDestinationFile(delta.getName());
            try {
                JBPatch.bspatch(sourceKML, destination, delta);
            }
            catch (Exception  e){
                e.printStackTrace();
            }
        }
    }


    private File getLatestDeltaFile(String source){
        File[] workingFiles = Environment.getExternalStoragePublicDirectory("/DMS/Working").listFiles();
        for(File file : workingFiles){
            if(file.getName().contains(".diff") && file.getName().contains(getAbsoluteFileName(source))){
                return file;
            }
        }
        return null;
    }

    private String getDeltaName(String fileName){
        String absoluteFileName = getAbsoluteFileName(fileName);
        int old_version = getLatestDiffVersionNo(fileName);
        int new_version = old_version++;
        String timestamp = KmzUtils.getTimeStamp();
        return absoluteFileName+new_version+"_"+timestamp+".diff";
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

    private String getAbsoluteFileName(String fileName){
        Pattern pattern = Pattern.compile("_");
        String[] result = pattern.split(fileName);
        String absoluteFileName = "";
        for(int i=0;i<9;i++){
            absoluteFileName = absoluteFileName+result[i]+"_";
        }
        return absoluteFileName;
    }

    private File getDestinationFile(String deltaName){
        String fileName=getAbsoluteFileName(deltaName);
        Pattern pattern = Pattern.compile("_");
        String result[] = pattern.split(deltaName);
        fileName = fileName+result[9]+".kml";
        return Environment.getExternalStoragePublicDirectory("DMS/Show/"+fileName);
    }

}
