package com.disarm.surakshit.pdm.Util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by naman on 24/12/17.
 */

public class KmzUtils {
    Context context;
    public KmzUtils(Context context){
        this.context = context;
    }

    private void extractKMLfromKMZ(File kmzFile){

    }

    public static String getTimeStamp(){
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }
}
