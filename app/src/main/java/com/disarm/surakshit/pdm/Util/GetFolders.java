package com.disarm.surakshit.pdm.Util;

import android.content.Context;
import android.os.Environment;

import com.disarm.surakshit.pdm.UI_Map;
import com.snatik.storage.Storage;

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

    public static File getShowDir(){
        File dir = Environment.getExternalStoragePublicDirectory("DMS/Show");
        return dir;
    }

    public static File getTmpKMZExtractForKMLDir(){
        File dir = Environment.getExternalStoragePublicDirectory("DMS/TmpKMZExtractForKML");
        Storage storage = new Storage(UI_Map.contextOfApplication);
        if(dir.exists())
            storage.deleteDirectory(dir.toString());
        dir.mkdir();
        return dir;
    }

    public static File getTmpKMZExtractForCopyToShow(){
        File dir = Environment.getExternalStoragePublicDirectory("DMS/TmpCopy");
        Storage storage = new Storage(UI_Map.contextOfApplication);
        if(dir.exists())
            storage.deleteDirectory(dir.toString());
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
