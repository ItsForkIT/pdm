package com.disarm.sanna.pdm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by arka on 16/9/16.
 */
public class ShareActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    ListView receivedFiles;
    String number;
    ArrayList<File> allFiles;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_activity);

        receivedFiles = (ListView)findViewById(R.id.share_received_files);
        Bundle extra = getIntent().getExtras();
        if(extra != null) {
            Senders sender = extra.getParcelable("SENDER_DATA");
            if(sender != null && sender.getNumber() != null) {
                number = sender.getNumber();
            }
            if(sender != null && sender.getAllFiles() != null) {
                allFiles = sender.getAllFiles();
            }
        }

        ArrayList<String> allFilesName = new ArrayList<>();
        for(File file: allFiles) {
            allFilesName.add(file.getName());
        }

        ArrayAdapter<String> chatListAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, allFilesName);
        receivedFiles.setAdapter(chatListAdapter);
        receivedFiles.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        File file = allFiles.get(position);
        Intent openFile = new Intent();
        openFile.setAction(Intent.ACTION_VIEW);
        openFile.setData(Uri.parse("" + file));

        if(file.getName().startsWith("IMG")) {
            openFile.setType("image/*");
        } else if(file.getName().startsWith("VID")) {
            openFile.setType("video/*");
        } else  if(file.getName().startsWith("TXT")) {
            openFile.setType("text/*");
        } else if(file.getName().startsWith("SVS")) {
            openFile.setType("audio/*");
        } else if(file.getName().startsWith("SMS")) {
            openFile.setType("vnd.android-dir/mms-sm");
        }

        startActivity(openFile);
    }
}
