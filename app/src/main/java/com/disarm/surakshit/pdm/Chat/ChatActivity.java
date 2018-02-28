package com.disarm.surakshit.pdm.Chat;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.disarm.surakshit.pdm.Chat.Holders.IncomingAudioHolders;
import com.disarm.surakshit.pdm.Chat.Holders.IncomingVideoHolders;
import com.disarm.surakshit.pdm.Chat.Holders.OutgoingAudioHolders;
import com.disarm.surakshit.pdm.Chat.Holders.OutgoingVideoHolders;
import com.disarm.surakshit.pdm.R;
import com.disarm.surakshit.pdm.Util.Params;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.io.File;

public class ChatActivity extends AppCompatActivity implements MessageHolders.ContentChecker<Message> {
    MessagesList messagesList;
    ImageLoader load;
    String number;
    MessagesListAdapter<Message> messagesListAdapter;
    private final byte CONTENT_AUDIO=1,CONTENT_VIDEO=2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messagesList = (MessagesList) findViewById(R.id.messagesList);
        load = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                File im = Environment.getExternalStoragePublicDirectory(url);
                if(im.exists()) {
                    Picasso.with(ChatActivity.this).load(im).resize(800,1000).centerCrop().into(imageView);
                }
            }
        };
        MessageHolders holders = new MessageHolders();
        holders.registerContentType(CONTENT_AUDIO,
                IncomingAudioHolders.class,R.layout.chat_incoming_audio,
                OutgoingAudioHolders.class,R.layout.chat_outgoing_audio,
                this);
        holders.registerContentType(CONTENT_VIDEO,
                IncomingVideoHolders.class,R.layout.chat_incoming_video,
                OutgoingVideoHolders.class,R.layout.chat_outgoing_video,
                this);
        number = getIntent().getStringExtra("number");
        messagesListAdapter = new MessagesListAdapter<Message>(Params.SOURCE_PHONE_NO,holders,load);
        messagesListAdapter.setOnMessageClickListener(new MessagesListAdapter.OnMessageClickListener<Message>() {
            @Override
            public void onMessageClick(Message message) {

                //Define what to do with msg touch events
                //Add only for map type and Image type
                if(message.isImage()){
                    Intent i = new Intent(ChatActivity.this, ImageViewActivity.class);
                    i.putExtra("url",message.getUrl());
                    startActivity(i);
                }
                
            }
        });
    }

    @Override
    public boolean hasContentFor(Message message, byte type) {
        if(type == CONTENT_AUDIO){
            return message.isAudio();
        }
        if(type == CONTENT_VIDEO){
            return message.isVideo();
        }
        return false;
    }
}
