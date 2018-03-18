package com.disarm.surakshit.pdm.Util;

import android.content.Context;
import android.os.Environment;

import com.snatik.storage.Storage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import ie.wombat.jbdiff.JBDiff;
import ie.wombat.jbdiff.JBPatch;

/**
 * Created by naman on 24/12/17.
 */

public class DiffUtils {


    public static boolean createDiff(File source,File destination) throws IOException {
        File delta = Environment.getExternalStoragePublicDirectory("/DMS/Working/SurakshitDiff/"+getDeltaName(source));
        try {
            JBDiff.bsdiff(source, destination, delta);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    public static boolean applyPatch(File source,File delta){
        if(delta!=null){
            File destination = getDestinationFile(delta);
            try {
                JBPatch.bspatch(source, destination, delta);
                return true;
            }
            catch (Exception  e){
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private static String getDeltaName(File source) throws IOException {
        File diffDir = Environment.getExternalStoragePublicDirectory("DMS/Working/SurakshitDiff");
        String name = FilenameUtils.getBaseName(source.getName());
        int version=0;
        for(File file : diffDir.listFiles()){
            if(file.getName().contains(name)){
                String s = FilenameUtils.getBaseName(file.getName());
                version = Integer.parseInt(s.substring(s.lastIndexOf("_")+1, s.length()));
                FileUtils.forceDelete(file);
                break;
            }
        }
        version++;
        return name+"_"+version+".diff";
    }

    private static File getDestinationFile(File delta){
        String deltaName = FilenameUtils.getBaseName(delta.getName());
        return Environment.getExternalStoragePublicDirectory("DMS/KML/Dest/LatestKml/"+deltaName+".kml");
    }
}
