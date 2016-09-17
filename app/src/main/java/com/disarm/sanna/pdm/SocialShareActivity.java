package com.disarm.sanna.pdm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by arka on 14/9/16.
 * Offline Social Share Activity
 */
public class SocialShareActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener{
    ListView chatList;
    ArrayList<File> allFiles;
    ArrayList<String> senderList;
    HashMap<String, Integer> numberToSenderMap;
    ArrayList<Senders> senders;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_social_share);

        chatList = (ListView)findViewById(R.id.list_social_share_chats);

        allFiles = new ArrayList<>();

        senderList = new ArrayList<>();
        numberToSenderMap = new HashMap<>();
        senders = new ArrayList<>();

        populateChatList();
    }

    /**
     * Find all files in working dirctory
     */
    private void findFiles() {

        File workingDirectory = new File(Environment.getExternalStorageDirectory().toString() +
                "/DMS/Working/");

        File[] files = workingDirectory.listFiles();
        if(files==null) {
            Toast.makeText(this, "Working Directory Not Found", Toast.LENGTH_LONG);
            return;
        }

        for(File file:files) {
            if( !file.isDirectory() && !file.isHidden() ) { // add all files which are not hidden
                allFiles.add(file);
            }
        }
    }

    /**
     * Populate chat list with the phone numbers of senders
     */
    private void populateChatList() {

        findFiles();
        for(File file:allFiles) {
            String name = file.getName();

            if(name.startsWith("MapDisarm")) { // ignore GPS trails for now
                continue;
            }

            if(senderList.contains(name.split("_")[3]) == false) {
                senderList.add(name.split("_")[3]);
                Senders sender = new Senders(name.split("_")[3]);
                senders.add(sender);
                numberToSenderMap.put(name.split("_")[3], senders.size()-1);
            } else if(name.split("_")[3].indexOf(".") == -1){
                Senders sender = senders.get(numberToSenderMap.get(name.split("_")[3]));

                sender.addFile(file);
                if(name.startsWith("IMG")) {
                    sender.addImage(file);
                } else if(name.startsWith("VID")) {
                    sender.addVideo(file);
                } else  if(name.startsWith("TXT")) {
                    sender.addText(file);
                } else if(name.startsWith("SVS")) {
                    sender.addRecording(file);
                } else if(name.startsWith("SMS")) {
                    sender.addSms(file);
                }
            }
        }

        ArrayAdapter<String> chatListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, senderList);
        chatList.setAdapter(chatListAdapter);
        chatList.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View view) {
    }

    /**
     * Start Chat and Share Activity based on the position
     * @param adapterView
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent shareActivityIntent = new Intent(this, ShareActivity.class);
        Senders sender = senders.get(position);
        shareActivityIntent.putExtra("SENDER_DATA", sender);
        startActivity(shareActivityIntent);
    }
}

