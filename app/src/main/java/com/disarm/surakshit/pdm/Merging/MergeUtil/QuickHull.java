package com.disarm.surakshit.pdm.Merging.MergeUtil;

/**
 * Created by bishakh on 6/30/18.
 */


import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuickHull {

    public ArrayList<GeoPoint> quickHull(List<GeoPoint> points) {
        if (points.size() <= 3) {
            ArrayList<GeoPoint> clonedList = new ArrayList<>();
            clonedList.ensureCapacity(points.size());
            Collections.copy(clonedList, points);
            return clonedList;
        }

        final ArrayList<GeoPoint> convexHull = new ArrayList<>();

        int minPoint = -1;
        int maxPoint = -1;
        double minX = Integer.MAX_VALUE;
        double maxX = Integer.MIN_VALUE;

        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).getLongitude() < minX) {
                minX = points.get(i).getLongitude();
                minPoint = i;
            }
            if (points.get(i).getLongitude() > maxX) {
                maxX = points.get(i).getLongitude();
                maxPoint = i;
            }
        }

        final GeoPoint a = points.get(minPoint);
        final GeoPoint b = points.get(maxPoint);
        convexHull.add(a);
        convexHull.add(b);
        points.remove(a);
        points.remove(b);

        ArrayList<GeoPoint> leftSet = new ArrayList<>();
        ArrayList<GeoPoint> rightSet = new ArrayList<>();

        for (GeoPoint p : points) {
            if (pointLocation(a, b, p) == -1) {
                leftSet.add(p);
            } else {
                rightSet.add(p);
            }
        }

        hullSet(a, b, rightSet, convexHull);
        hullSet(b, a, leftSet, convexHull);

        return convexHull;
    }

    private static void hullSet(GeoPoint a, GeoPoint b, ArrayList<GeoPoint> set, ArrayList<GeoPoint> convexHull) {
        final int insertPosition = convexHull.indexOf(b);
        if (set.size() == 0) return;
        if (set.size() == 1) {
            final GeoPoint p = set.get(0);
            set.remove(p);
            convexHull.add(insertPosition, p);
            return;
        }
        double dist = Integer.MIN_VALUE;
        int furthestPoint = -1;
        for (int i = 0; i < set.size(); i++) {
            GeoPoint p = set.get(i);
            double distance = distance(a, b, p);
            if (distance > dist) {
                dist = distance;
                furthestPoint = i;
            }
        }

        final GeoPoint p = set.get(furthestPoint);
        set.remove(furthestPoint);
        convexHull.add(insertPosition, p);

        // Determine who's to the left of AP
        final ArrayList<GeoPoint> leftSetAP = new ArrayList<>();
        for (GeoPoint m : set) {
            if (pointLocation(a, p, m) == 1) {
                leftSetAP.add(m);
            }
        }

        // Determine who's to the left of PB
        final ArrayList<GeoPoint> leftSetPB = new ArrayList<>();
        for (GeoPoint m : set) {
            if (pointLocation(p, b, m) == 1) {
                leftSetPB.add(m);
            }
        }

        hullSet(a, p, leftSetAP, convexHull);
        hullSet(p, b, leftSetPB, convexHull);
    }

    private static double distance(GeoPoint a, GeoPoint b, GeoPoint c) {
        final double ABx = b.getLongitude() - a.getLongitude();
        final double ABy = b.getLatitude() - a.getAltitude();
        double dist = ABx * (a.getLatitude() - c.getLatitude()) - ABy * (a.getLongitude() - c.getLongitude());
        if (dist < 0) dist = -dist;
        return dist;
    }

    private static int pointLocation(GeoPoint a, GeoPoint b, GeoPoint p) {
        double cp1 = (b.getLatitude() - a.getLatitude()) * (p.getLongitude() - a.getLongitude()) - (b.getLongitude() - a.getLongitude()) * (p.getLatitude() - a.getLatitude());
        return (cp1 > 0) ? 1 : -1;
    }
}