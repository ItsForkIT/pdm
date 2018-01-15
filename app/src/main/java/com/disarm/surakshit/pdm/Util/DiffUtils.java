package com.disarm.surakshit.pdm.Util;

import android.content.Context;
import android.os.Environment;

import com.snatik.storage.Storage;

import org.osmdroid.views.overlay.infowindow.InfoWindow;

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
    public boolean createDiff(File source,File destination){
        File tmpKMZExtractDir = GetFolders.getTmpKMZExtractForKMLDir();
        UnZip unZip = new UnZip(tmpKMZExtractDir.getPath()+"/",source.toString());
        File sourceKML = Environment.getExternalStoragePublicDirectory(tmpKMZExtractDir.getPath()+"/index.kml");
        File delta = Environment.getExternalStoragePublicDirectory("/DMS/Working/"+getDeltaName(source.getName()));
        try {
            JBDiff.bsdiff(sourceKML, destination, delta);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //Source file KMZ format
    public boolean applyPatch(File source,File delta){
        File tmpKMZExtractDir = GetFolders.getTmpKMZExtractForKMLDir();
        UnZip unZip = new UnZip(tmpKMZExtractDir.getPath()+"/",source.toString());
        File sourceKML = Environment.getExternalStoragePublicDirectory(tmpKMZExtractDir.getPath()+"/index.kml");
        if(delta!=null){
            File destination = getDestinationFile(delta.getName());
            try {
                JBPatch.bspatch(sourceKML, destination, delta);
                return true;
            }
            catch (Exception  e){
                e.printStackTrace();
                return false;
            }
        }
        return false;
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
            if(workingFiles[i].getName().contains(absoluteFileName) && workingFiles[i].getName().contains("diff")){
                String oldDiffName = workingFiles[i].getName();
                Pattern pattern = Pattern.compile("_");
                String[] result = pattern.split(oldDiffName);
                version_number = Integer.parseInt(result[9]);
            }
        }
        return version_number;
    }

    public String getAbsoluteFileName(String fileName){
        Pattern pattern = Pattern.compile("_");
        String[] result = pattern.split(fileName);
        String absoluteFileName = "";
        for(int i=0;i<8;i++){
            absoluteFileName = absoluteFileName+result[i]+"_";
        }
        char[] last = result[8].toCharArray();
        String groupID="";
        for(int i=0;i<last.length;i++){
            if(last[i]>='0' && last[i]<='9'){
                groupID = groupID + last[i];
            }
            else
                break;
        }
        return absoluteFileName+groupID+"_";
    }


    private File getDestinationFile(String deltaName){
        String fileName=getAbsoluteFileName(deltaName);
        Pattern pattern = Pattern.compile("_");
        String result[] = pattern.split(deltaName);
        fileName = fileName+result[9]+"_"+result[10]+".kml";
        return Environment.getExternalStoragePublicDirectory("DMS/Show/"+fileName);
    }

    public File getSourceOfDelta(String delta_name){
        File working = GetFolders.getWorkingDir();
        for(File file : working.listFiles()){
            if(file.getName().contains(".kmz") && delta_name.contains(getAbsoluteFileName(file.getName()))){
                return file;
            }
        }
        return null;
    }

    public boolean compareDiffVersionWithShowDirKMLVersion(String deltaName){
        File show = GetFolders.getShowDir();
        for(File file : show.listFiles()){
            if(file.getName().contains(getAbsoluteFileName(deltaName))){
                Pattern pattern = Pattern.compile("_");
                String[] showName = pattern.split(file.getName());
                String[] delta = pattern.split(deltaName);
                int s_version = Integer.parseInt(showName[9]);
                int d_version = Integer.parseInt(delta[9]);
                if(s_version==d_version){
                    return true;
                }
                else{
                    return false;
                }
            }
        }
        return false;
    }
    //Without _
    public static String absoluteFileName(String fileName){
        Pattern pattern = Pattern.compile("_");
        String[] result = pattern.split(fileName);
        String absoluteFileName = "";
        for(int i=0;i<8;i++){
            absoluteFileName = absoluteFileName+result[i]+"_";
        }
        char[] last = result[8].toCharArray();
        String groupID="";
        for(int i=0;i<last.length;i++){
            if(last[i]>='0' && last[i]<='9'){
                groupID = groupID + last[i];
            }
            else
                break;
        }
        return absoluteFileName+groupID;
    }
}
