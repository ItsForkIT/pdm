package com.disarm.sanna.pdm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.clustering.StaticCluster;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.HashMap;

/**
 * Created by naman on 29/8/17.
 */

public class MyMarkerCluster extends RadiusMarkerClusterer {
    Context context;
    public HashMap<Marker,String> fileAddress;
    public MyMarkerCluster(Context ctx) {
        super(ctx);
        context=ctx;
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
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupview = inflater.inflate(R.layout.popup_map,null);
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT;
                PopupWindow pw = new PopupWindow(popupview,width,height,true);
                pw.showAtLocation(mapView,Gravity.CENTER,width,height);

                for(int i=0;i<cluster.getSize();i++){
                    Marker m = cluster.getItem(i);
                    if(fileAddress.containsKey(m)){

                    }
                }
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
