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

    private void createDiff(File source,File destination){
        Storage storage = new Storage(context);

    }



    private void getDeltaName(String fileName){

    }

    private void getLatestDiffVersionNo(String fileName){
        File working = GetFolders.getWorkingDir();
        File[] workingFiles = working.listFiles();
        String absoluteFileName = getAbsoluteFileName(fileName);
    }

    private String getAbsoluteFileName(String fileName){
        Pattern pattern = Pattern.compile("_");
        String[] result = pattern.split(fileName);
        String absoluteFileName = "";
        for(int i=0;i<9;i++){
            absoluteFileName = absoluteFileName+result[i];
        }
        return absoluteFileName;
    }
}
