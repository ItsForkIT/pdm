package com.disarm.surakshit.pdm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.disarm.surakshit.pdm.Util.UnZip;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.io.File;

/**
 * Created by naman on 30/10/17.
 */

public class CustomInfoWindow extends InfoWindow {

    String title_str,latlon_str,file_name;
    Context context;
    /**
     * @param layoutResId the id of the view resource.
     * @param mapView     the mapview on which is hooked the view
     */
    public CustomInfoWindow(int layoutResId, MapView mapView,String title_str,String latlon_str,String file_name) {
        super(layoutResId, mapView);
        this.title_str = title_str;
        this.latlon_str = latlon_str;
        this.file_name = file_name;
        this.context = UI_Map.contextOfApplication;
    }

    @Override
    public void onOpen(Object item) {
        Drawable draw = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                draw = context.getDrawable(R.drawable.marker_red);
        }
        if(item instanceof Polygon)
            ((Polygon)item).setStrokeColor(Color.BLACK);
        if(item instanceof Marker)
            ((Marker)item).setIcon(draw);
        Button open = (Button) mView.findViewById(R.id.btn_iw_open);
        TextView title = (TextView) mView.findViewById(R.id.tv_iw_title);
        TextView latlon = (TextView) mView.findViewById(R.id.tv_iw_latlon);
        latlon.setText(latlon_str);
        if(file_name.contains("TXT")){
            open.setVisibility(View.GONE);
            title.setText(title_str);
        }
        else{
            int start_index=0,end_index=0;
            String description = "";
            for(int i=2;i<title_str.length();i++){
                if(title_str.charAt(i-2)=='<'&&title_str.charAt(i-1)=='p'&&title_str.charAt(i)=='>'){
                    start_index = i+1;
                }
                if(i!=2){
                    if(title_str.charAt(i-3)=='<'&&title_str.charAt(i-2)=='/'&&title_str.charAt(i)=='>'&&title_str.charAt(i-1)=='p'){
                        end_index = i-3;
                    }
                }
            }
            description = title_str.substring(start_index,end_index);
            title.setText(description);
        }
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File destFolder  = Environment.getExternalStoragePublicDirectory("DMS/tmpOpen/");
                if(!destFolder.exists()){
                    destFolder.mkdir();
                }
                File sourceFile = Environment.getExternalStoragePublicDirectory("DMS/Working/"+file_name);
                UnZip unzip = new UnZip(destFolder.getPath().toString()+"/",sourceFile.toString());
                destFolder  = Environment.getExternalStoragePublicDirectory("DMS/tmpOpen");
                String absolute_file_name = "";
                for(int i=0;i<file_name.length();i++){
                    if(file_name.charAt(i)=='.'){
                        break;
                    }
                    else{
                        absolute_file_name = absolute_file_name + file_name.charAt(i);
                    }
                }
                File[] files = destFolder.listFiles();
                for(File file : files){
                    if(file.getName().contains(absolute_file_name)){
                        openFile(file);
                    }
                }
            }
        });

    }

    @Override
    public void onClose() {

    }


    private void openFile(File file) {
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        String type = map.getMimeTypeFromExtension(ext);

        if (type == null)
            type = "*/*";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri data = FileProvider.getUriForFile(context,context.getPackageName()+".provider",file);

        intent.setDataAndType(data, type);
        try{
            context.startActivity(intent);
        }
        catch (Exception e){
            Toast.makeText(context,"Sorry !!! Can't open file", Toast.LENGTH_LONG).show();
        }
    }
}
