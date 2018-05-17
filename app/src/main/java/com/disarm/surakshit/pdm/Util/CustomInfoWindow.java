package com.disarm.surakshit.pdm.Util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import com.disarm.surakshit.pdm.Chat.ImageViewActivity;
import com.disarm.surakshit.pdm.Chat.VideoPlayer;
import com.disarm.surakshit.pdm.R;


import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.io.File;
import java.io.IOException;


public class CustomInfoWindow extends InfoWindow {
    Polygon polygon;
    Marker marker;
    Context context;
    ListView lv;
    public CustomInfoWindow(int layoutResId, MapView mapView, Polygon polygon, Context context) {
        super(layoutResId, mapView);
        this.polygon = polygon;
        this.context = context;
    }

    public CustomInfoWindow(int layoutResId, MapView mapView, Marker marker, Context context) {
        super(layoutResId, mapView);
        this.marker = marker;
        this.context = context;
    }

    @Override
    public void onOpen(Object item) {
        lv = mView.findViewById(R.id.ciw_lv);
        String des;
        if(item instanceof Polygon)
            des = polygon.getSnippet();
        else
            des = marker.getSnippet();
        try {
            String[] files;
            if (des.contains(";"))
                files = des.split(";");
            else {
                files = new String[1];
                files[0] = des;
            }
            final String[] allFiles = files;
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, files);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String fileName = allFiles[i];
                    String folderName = "DMS/Working/";
                    if (fileName.contains("jpeg")) {
                        folderName = folderName + "SurakshitImages/"+fileName;
                        Intent ii = new Intent(context, ImageViewActivity.class);
                        ii.putExtra("url",folderName);
                        context.startActivity(ii);
                    }
                    else if (fileName.contains("mp4")) {
                        folderName = folderName + "SurakshitVideos/"+fileName;
                        Intent ii = new Intent(context, VideoPlayer.class);
                        ii.putExtra("url",folderName);
                        context.startActivity(ii);
                    }

                }
            });
        }
        catch (Exception e){
            String[] s = new String[1];
            s[0] = "No files found";
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, s);
            lv.setAdapter(adapter);
        }
    }

    @Override
    public void onClose() {

    }
}

