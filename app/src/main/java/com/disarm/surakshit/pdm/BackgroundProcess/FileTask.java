package com.disarm.surakshit.pdm.BackgroundProcess;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

//import com.disarm.sanna.pdm.ShareActivity;
import com.disarm.surakshit.pdm.UI_Map;
import com.disarm.surakshit.pdm.Util.KmzCreator;
import com.disarm.surakshit.pdm.location.MLocation;
import com.snatik.storage.Storage;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import static com.disarm.surakshit.pdm.Capture.Photo.TMP_FOLDER;
import static com.disarm.surakshit.pdm.SelectCategoryActivity.SOURCE_PHONE_NO;


/**
 * Created by Sanna on 05-07-2016.
 */
public class FileTask extends AsyncTask {
    String fileType, groupType, timestamp, ttl, dest, source, fileFormat;
    String[] fileName;
    public static final String GROUPID = "Group No";
    Context applicationContext = UI_Map.getContextOfApplication();
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    int idNumber, groupID;
    private boolean increaseSession = false;


    @Override
    protected synchronized void onPreExecute() {
        super.onPreExecute();

        if (applicationContext != null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        }

        editor = prefs.edit();
        idNumber = prefs.getInt(GROUPID, 0);
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        ttl = (String) objects[0];
        dest = (String) objects[1];
        source = SOURCE_PHONE_NO;
        File logFile = null;
        String state = Environment.getExternalStorageState();
        File dir = Environment.getExternalStoragePublicDirectory("DMS/Working");
        Log.v("Logger Initiated", "");
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (!dir.exists()) {
                Log.d("Dir created ", "Dir created ");
                dir.mkdirs();
            }
            File[] foundFiles = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {

                    return name.startsWith("MapDisarm_Log_");
                }
            });

            Log.v("File with MapDisarm_Log", foundFiles.length + "");

            if (foundFiles != null && foundFiles.length > 0) {
                logFile = new File(foundFiles[0].toString());
                Log.v("LogFile:", foundFiles[0].toString());
            }

        }
        String latlng = getloc(applicationContext);

        String pathFrom = applicationContext.getExternalFilesDir(TMP_FOLDER).toString();
        String pathTo = Environment.getExternalStorageDirectory().toString() + "/DMS/Working";
        Log.d("Files", "Path: " + pathFrom);
        File f = new File(pathFrom);
        File file[] = f.listFiles();
        Log.d("No. Files", String.valueOf(file.length));
        if (file.length > 0) {
            increaseSession = true;
        }
        for (int i = 0; i < file.length; i++) {
            fileName = file[i].getName().split("_");
            fileType = fileName[0];
            groupType = "data";
            //groupType = fileName[1];
            timestamp = fileName[2];
            fileFormat = fileName[3];
            groupID = idNumber;
            Log.v("FileNames", fileType + ttl + groupType + source + dest + latlng + timestamp + groupID);
            File from = new File(pathFrom, file[i].getName());
            String acutalFileName = fileType + "_" +
                    ttl + "_" +
                    groupType + "_" +
                    source + "_" +
                    dest + "_" +
                    latlng + "_" +
                    timestamp + "_" +
                    groupID +
                    fileFormat;
            String actualKmzName = fileType + "_" +
                    ttl + "_" +
                    groupType + "_" +
                    source + "_" +
                    dest + "_" +
                    latlng + "_" +
                    timestamp + "_" +
                    groupID;
            File to = new File(pathTo, acutalFileName);
            Log.d("File type", "File format :" + fileFormat);
            KmlDocument kml = new KmlDocument();
            String description = (String) objects[4];
            ArrayList<GeoPoint> polygon_points = (ArrayList) objects[2];
            if (polygon_points.size() == 1) {
                GeoPoint point = polygon_points.get(0);
                MapView map = (MapView) objects[3];
                Marker m = new Marker(map);
                m.setPosition(point);
                m.setTitle("Point");
                if (fileFormat.equals(".jpg")) {
                    m.setSnippet("<img source='" + acutalFileName + "'>\n<p>" + description + "</p>");
                } else if (fileFormat.equals(".mp4")) {
                    m.setSnippet("<video width=\"320\" height=\"240\" controls/>\n" +
                            "<source src=\"" + acutalFileName + "\" type=\"video/mp4\">\n" +
                            "<source src=\"" + actualKmzName + ".ogg\" type=\"video/ogg\">\n" +
                            "Your browser does not support the video tag.\n" +
                            "</video>\n<p>" + description + "</p>");
                } else if (fileFormat.equals(".mp3")) {
                    m.setSnippet("<audio controls>\n" +
                            "  <source src=\"" + acutalFileName + "\" type=\"audio/mpeg\">\n" +
                            "  <source src=\"" + actualKmzName + ".ogg\" type=\"audio/ogg\">\n" +
                            "Your browser does not support the audio element.\n" +
                            "</audio>\n<p>" + description + "</p>");

                    Log.d("Snippet", "Audio snippet");
                }
                KmlPlacemark place = new KmlPlacemark(m);
                kml.mKmlRoot.add(place);
                kml.mKmlRoot.setExtendedData("Media Type", fileType);
                kml.mKmlRoot.setExtendedData("Group Type", groupType);
                kml.mKmlRoot.setExtendedData("Time Stamp", timestamp);
                kml.mKmlRoot.setExtendedData("Source", source);
                kml.mKmlRoot.setExtendedData("Destination", dest);
                kml.mKmlRoot.setExtendedData("Lat Long", latlng);
                kml.mKmlRoot.setExtendedData("Group ID", groupID + "");
                kml.mKmlRoot.setExtendedData("Priority", ttl);
                kml.mKmlRoot.setExtendedData("KML Type", "Point");
            } else {
                Polygon polygon = new Polygon();
                polygon_points.add(polygon_points.get(0));
                polygon.setPoints(polygon_points);
                polygon.setTitle("Polygon");
                if (fileFormat.equals(".jpg")) {
                    polygon.setSnippet("<img source='" + acutalFileName + "'>\n<p>" + description + "</p>");
                } else if (fileFormat.equals(".mp4")) {
                    polygon.setSnippet("<video width=\"320\" height=\"240\" controls/>\n" +
                            "<source src=\"" + acutalFileName + "\" type=\"video/mp4\">\n" +
                            "<source src=\"" + actualKmzName + ".ogg\" type=\"video/ogg\">\n" +
                            "Your browser does not support the video tag.\n" +
                            "</video>\n<p>" + description + "</p>");
                } else if (fileFormat.equals(".mp3")) {
                    polygon.setSnippet("<audio controls>\n" +
                            "  <source src=\"" + acutalFileName + "\" type=\"audio/mpeg\">\n" +
                            "  <source src=\"" + actualKmzName + ".ogg\" type=\"audio/ogg\">\n" +
                            "Your browser does not support the audio element.\n" +
                            "</audio>\n<p>" + description + "</p>");

                    Log.d("Snippet", "Audio snippet");
                }
                kml.mKmlRoot.setExtendedData("Media Type", fileType);
                kml.mKmlRoot.setExtendedData("Group Type", groupType);
                kml.mKmlRoot.setExtendedData("Time Stamp", timestamp);
                kml.mKmlRoot.setExtendedData("Source", source);
                kml.mKmlRoot.setExtendedData("Destination", dest);
                kml.mKmlRoot.setExtendedData("Lat Long", latlng);
                kml.mKmlRoot.setExtendedData("Group ID", groupID + "");
                kml.mKmlRoot.setExtendedData("Priority", ttl);
                kml.mKmlRoot.setExtendedData("KML Type", "Polygon");
                kml.mKmlRoot.addOverlay(polygon, kml);
            }
            //change kml file name
            File kmlFile = new File(pathTo, actualKmzName + ".kml");
            kml.saveAsKML(kmlFile);
            //save kml in a separate folder until uploaded
            File fileTempKml = Environment.getExternalStoragePublicDirectory("DMS/tmpKML/" + actualKmzName + ".kml");
            kml.saveAsKML(fileTempKml);

            from.renameTo(to);
            Storage storage = new Storage(applicationContext);
            File tempKmzFolder = Environment.getExternalStoragePublicDirectory("DMS/tmpKMZ");
            if (!tempKmzFolder.exists()) {
                tempKmzFolder.mkdir();
            }
            storage.move(to.getPath(), tempKmzFolder.getPath() + "/" + to.getName());
            storage.move(kmlFile.getPath(), tempKmzFolder.getPath() + "/" + kmlFile.getName());
            KmzCreator kmz = new KmzCreator();
            kmz.zipIt(Environment.getExternalStoragePublicDirectory("DMS/Working/" + actualKmzName + ".kmz").toString());
            storage.deleteDirectory(tempKmzFolder.toString());
            UI_Map.setWorkingData(true);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (increaseSession) {
            idNumber += 1;
            editor.putInt(GROUPID, idNumber);
            editor.commit();
        }
    }

    public static String getloc(Context context) {
        Location l = MLocation.getLocation(context);
        String lat_long = null;
        if (l != null) {
            double lat = l.getLatitude();
            double lon = l.getLongitude();
            boolean hasLatLon = (lat != 0.0d) || (lon != 0.0d);
            if (hasLatLon) {
                Log.v("lat_lon", String.valueOf(l.getLatitude() + "_" + l.getLongitude()));
                lat_long = String.valueOf(l.getLatitude() + "_" + l.getLongitude());
            }
        }
        return lat_long;
    }
}
