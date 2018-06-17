package com.disarm.surakshit.pdm.Util;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.BottomSheetBehavior;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.disarm.surakshit.pdm.Chat.ImageViewActivity;
import com.disarm.surakshit.pdm.Chat.VideoPlayer;
import com.disarm.surakshit.pdm.Fragments.BottomListAdapter;
import com.disarm.surakshit.pdm.R;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

/**
 * Created by AmanKumar on 6/14/2018.
 */

public class BottomCustomWindow extends InfoWindow {
    Polygon polygon;
    Marker marker;
    Context context;
    BottomSheetBehavior bottomSheetBehavior;
    LinearLayout view;
    TextView nameText;
    ListView listView;
    BottomListAdapter adapter;
    String[] allFiles;

    public BottomCustomWindow(int layoutResId, MapView mapView, Polygon polygon, Context context, BottomSheetBehavior bottomSheetBehavior, LinearLayout view) {
        super(layoutResId, mapView);
        this.polygon = polygon;
        this.context = context;
        this.bottomSheetBehavior = bottomSheetBehavior;
        this.view = view;
        nameText = view.findViewById(R.id.bottom_overlay_name);
        listView = view.findViewById(R.id.bottom_list_view);
    }

    public BottomCustomWindow(int layoutResId, MapView mapView, Marker marker, Context context, BottomSheetBehavior bottomSheetBehavior, LinearLayout view) {
        super(layoutResId, mapView);
        this.marker = marker;
        this.context = context;
        this.bottomSheetBehavior = bottomSheetBehavior;
        this.view = view;
        nameText = view.findViewById(R.id.bottom_overlay_name);
        listView = view.findViewById(R.id.bottom_list_view);
    }

    @Override
    public void onOpen(Object item) {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }, 500);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        listScrollHelper();
        nameText.setText("Gis Name");
        String des;
        if (item instanceof Polygon)
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
            allFiles = files;
            adapter = new BottomListAdapter(context, files);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            String[] s = new String[1];
            s[0] = "No files found";
            adapter = new BottomListAdapter(context, s);
            listView.setAdapter(adapter);
            allFiles = s;
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String fileAndTime = allFiles[i];
                String fileName[] = fileAndTime.split("-");
                String folderName = "DMS/Working/";
                if (fileName[0].contains("jpeg")) {
                    folderName = folderName + "SurakshitImages/" + fileName[0];
                    Intent ii = new Intent(context, ImageViewActivity.class);
                    ii.putExtra("url", folderName);
                    context.startActivity(ii);
                } else if (fileName[0].contains("mp4")) {
                    folderName = folderName + "SurakshitVideos/" + fileName[0];
                    Intent ii = new Intent(context, VideoPlayer.class);
                    ii.putExtra("url", folderName);
                    context.startActivity(ii);
                }
            }
        });
    }

    @Override
    public void onClose() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        adapter = null;
    }

    private void listScrollHelper() {
        listView.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow NestedScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow NestedScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });
    }
}

