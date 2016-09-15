package com.disarm.sanna.pdm;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

/**
 * Created by arka on 14/9/16.
 * Offline Social Share Activity
 */
public class SocialShareActivity extends AppCompatActivity {
    ListView chatList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_social_share);

        chatList = (ListView)findViewById(R.id.list_social_share_chats);

        populateChatList();
    }

    /**
     * Populate chat list with the phone numbers of senders
     */
    private void populateChatList() {
    }
}
