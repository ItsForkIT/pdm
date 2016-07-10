package com.disarm.sanna.pdm.GPS;

/**
 * Created by Sanna on 13-06-2016.
 */
public class CalculateBearing {

    public static double bearing(double slat1, double slong1, double lat2, double lon2)
    {

        double longDiff= lon2-slong1;
        double y = Math.sin(longDiff)*Math.cos(lat2);
        double x = Math.cos(slat1)*Math.sin(lat2)-Math.sin(slat1)*Math.cos(lat2)*Math.cos(longDiff);

        double bearingValue = ( Math.toDegrees(Math.atan2(y, x)) + 360 ) % 360;

        return bearingValue;
    }
}
