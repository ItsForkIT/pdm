package com.disarm.sanna.pdm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.clustering.StaticCluster;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by naman on 29/8/17.
 */

public class MyMarkerCluster extends RadiusMarkerClusterer {
    Context context;
    ListView lvmap;
    public HashMap<Marker,String> fileAddress;
    public MyMarkerCluster(Context ctx,ListView lvmap) {
        super(ctx);
        context=ctx;
        this.lvmap = lvmap;
    }

    @Override
    public Marker buildClusterMarker(final StaticCluster cluster, MapView mapView) {
        final Marker m = new Marker(mapView);
        m.setPosition(cluster.getPosition());
        m.setInfoWindow(null);
        m.setAnchor(mAnchorU, mAnchorV);
        m.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                ArrayList<String> al = new ArrayList<String>();
                for(int i=0;i<cluster.getSize();i++){
                    Marker m = cluster.getItem(i);
                    if(fileAddress.containsKey(m)){
                        al.add(fileAddress.get(m));
                    }
                }
                ArrayAdapter<String> ad = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,al);
                lvmap.setAdapter(ad);
                return false;
            }
        });
        Bitmap finalIcon = Bitmap.createBitmap(mClusterIcon.getWidth(), mClusterIcon.getHeight(), mClusterIcon.getConfig());
        Canvas iconCanvas = new Canvas(finalIcon);
        iconCanvas.drawBitmap(mClusterIcon, 0, 0, null);
        String text = "" + cluster.getSize();
        int textHeight = (int) (mTextPaint.descent() + mTextPaint.ascent());
        iconCanvas.drawText(text,
                mTextAnchorU * finalIcon.getWidth(),
                mTextAnchorV * finalIcon.getHeight() - textHeight / 2,
                mTextPaint);
        m.setIcon(new BitmapDrawable(mapView.getContext().getResources(), finalIcon));
        return m;
    }

    public void addMarker(Marker m, String path){
        mItems.add(m);
        fileAddress.put(m,path);
    }
}
