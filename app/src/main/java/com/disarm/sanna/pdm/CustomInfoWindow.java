package com.disarm.sanna.pdm;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.disarm.sanna.pdm.Util.UnZip;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.io.File;
import java.util.regex.Pattern;

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
        Button open = (Button) mView.findViewById(R.id.btn_iw_open);
        TextView title = (TextView) mView.findViewById(R.id.tv_iw_title);
        TextView latlon = (TextView) mView.findViewById(R.id.tv_iw_latlon);
        latlon.setText(latlon_str);
        if(file_name.contains("kml")){
            open.setVisibility(View.GONE);
            title.setText(title_str);
        }
        else{
            String description = "";
            for(int i=2;i<title_str.length();i++){

            }
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
