package com.disarm.surakshit.pdm.Fragments;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.disarm.surakshit.pdm.R;
import com.disarm.surakshit.pdm.Util.ContactUtil;
import com.disarm.surakshit.pdm.Util.Params;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by AmanKumar on 6/15/2018.
 */

public class BottomListAdapter extends ArrayAdapter<String> {
    Context context;
    String[] files;

    public BottomListAdapter(@NonNull Context context, String[] files) {
        super(context, 0, files);
        this.context = context;
        this.files = files;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View listItem = view;
        if (listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_list_item, parent, false);
        TextView name = listItem.findViewById(R.id.bottom_list_media_name);
        TextView timeName = listItem.findViewById(R.id.bottom_list_media_date);
        String fileAndTime = files[position];
        String f[];
        String file;
        if (fileAndTime.contains("-")) {
            f = fileAndTime.split("-");
            file = f[0];
            SimpleDateFormat fmtOut = new SimpleDateFormat("dd-MM-yyyy");
            Date date = new Date();
            date.setTime(Long.parseLong(f[1]));
            timeName.setText(fmtOut.format(date));
        } else
            file = fileAndTime;
        String values[];
        String text = file;
        if (file.contains("_")) {
            values = file.split("_");
            if (values[1].equals(Params.SOURCE_PHONE_NO))
                text = "To " + ContactUtil.getContactName(context, values[2]);
            else
                text = "From " + ContactUtil.getContactName(context, values[1]);
        }
        name.setText(text);
        ImageView imageView = listItem.findViewById(R.id.bottom_list_image);
        if (file.contains("jpeg")) {
            imageView.setImageResource(R.drawable.image);
        } else if (file.contains("mp4")) {
            imageView.setImageResource(R.drawable.video);
        } else if (file.contains("3gp")) {
            imageView.setImageResource(R.drawable.audio);
        }
        return listItem;

    }
}
