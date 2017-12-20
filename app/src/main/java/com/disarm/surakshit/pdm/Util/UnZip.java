package com.disarm.surakshit.pdm.Util;

/**
 * Created by naman on 28/10/17.
 */
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by naman on 28/10/17.
 */

public class UnZip {

    public UnZip(String unzipPath,String sourcePath){
        this.unzip(unzipPath,sourcePath);
    }

    private void unzip(String unzipPath,String sourcePath){
        Log.i("Unzip Path", unzipPath.toString());
        Log.i("Source Path", sourcePath.toString());
        String extractDir = unzipPath;
        final int BUFFER = 2048;
        try {
            BufferedOutputStream dest = null;
            FileInputStream fis = new FileInputStream(new File(sourcePath));
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(extractDir + entry.getName());


                if (file.exists()) {
                    continue;
                }
                if (entry.isDirectory()) {
                    if (!file.exists())
                        file.mkdirs();

                    continue;
                }
                int count;
                byte data[] = new byte[BUFFER];

                FileOutputStream fos = new FileOutputStream(file);
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
            zis.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}

