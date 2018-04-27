package com.disarm.surakshit.pdm.Util;

import android.os.Environment;

import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LatLonUtil {
    static GeoPoint center = null;

    public static GeoPoint getBoundaryOfTiles(){
        if(center != null){
            return center;
        }
        File zipFile = Environment.getExternalStoragePublicDirectory("osmdroid/tiles.zip");
        ZipFile f;
        try {
            f = new ZipFile(zipFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        Enumeration<? extends ZipEntry> entries = f.entries();

        Integer minZoomLevel = 50;
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            if(entry.isDirectory()){
                String entryname = entry.getName();
                if(entryname.split(Pattern.quote(File.separator)).length == 2){
                    String paths[] = entryname.split(Pattern.quote(File.separator));
                    Integer zoomLevel = Integer.parseInt(paths[paths.length-1]);
                    if(zoomLevel < minZoomLevel){
                        minZoomLevel = zoomLevel;
                    }
                }
            }
        }

        int maxX = 0, minX = 999999;
        entries = f.entries();
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();

            String entryName[] = entry.getName().split("/");

            if(entry.isDirectory() && entryName.length == 3 && Integer.parseInt(entryName[entryName.length - 2]) == minZoomLevel){
                Integer xval = Integer.parseInt(entryName[entryName.length - 1]);
                if(xval > maxX){
                    maxX = xval;
                }
                if(xval < minX){
                    minX = xval;
                }
            }
        }

        int minXmaxY = 0, minXminY = 9999999, maxXmaxY = 0,maxXminY = 99999999;
        entries = f.entries();
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();

            String entryName[] = entry.getName().substring(0, entry.getName().length() - 4).split("/");
            if(entry.getName().startsWith("tiles" + File.separator + minZoomLevel + File.separator + minX) && entryName.length == 4){
                int thisy = Integer.parseInt(entryName[entryName.length - 1]);
                if(thisy < minXminY){
                    minXminY = thisy;
                }
                if(thisy > minXmaxY){
                    minXmaxY = thisy;
                }
            }
            if(entry.getName().startsWith("tiles" + File.separator + minZoomLevel + File.separator + maxX) && entryName.length == 4){
                int thisy = Integer.parseInt(entryName[entryName.length - 1]);
                if(thisy < maxXminY){
                    maxXminY = thisy;
                }
                if(thisy > maxXmaxY){
                    maxXmaxY = thisy;
                }
            }
        }
        center = new GeoPoint(tile2lat((minXmaxY+minXminY+maxXmaxY+maxXminY)/4,minZoomLevel),tile2lon((minX+maxX)/2,minZoomLevel));
        return center;
    }


    private static double tile2lon(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    private static double tile2lat(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }
}
