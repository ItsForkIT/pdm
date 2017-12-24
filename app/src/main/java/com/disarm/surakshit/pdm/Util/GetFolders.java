package com.disarm.surakshit.pdm.Util;

import android.os.Environment;

import java.io.File;

/**
 * Created by naman on 24/12/17.
 */

public class GetFolders {
    public GetFolders(){

    }

    public static File getWorkingDir(){
        File dir = Environment.getExternalStoragePublicDirectory("DMS/Working");
        return dir;
    }

    public static File getTmpKMZExtractForKMLDir(){
        File dir = Environment.getExternalStoragePublicDirectory("DMS/TmpKMZExtractForKML");
        if(!dir.exists())
            dir.mkdir();
        return dir;
    }

    public static File getDiffDir(){
        File dir = Environment.getExternalStoragePublicDirectory("DMS/CreateDiff");
        if(!dir.exists())
            dir.mkdir();
        return dir;
    }

    public static File getPatchDir(){
        File dir = Environment.getExternalStoragePublicDirectory("DMS/PatchDiff");
        if(!dir.exists())
            dir.mkdir();
        return dir;
    }

    public static File getTmpMediaDir(){
        File dir = Environment.getExternalStoragePublicDirectory("DMS/tmpMedia");
        if(!dir.exists())
            dir.mkdir();
        return dir;
    }


}
