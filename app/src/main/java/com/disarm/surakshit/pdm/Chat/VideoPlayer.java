package com.disarm.surakshit.pdm.Chat;

import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.afollestad.materialdialogs.MaterialDialog;
import com.disarm.surakshit.pdm.R;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class VideoPlayer extends AppCompatActivity implements EasyVideoCallback {

    private EasyVideoPlayer player;
    MaterialDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        player = (EasyVideoPlayer) findViewById(R.id.video_player);
        player.setCallback(this);
        String source = getIntent().getStringExtra("url");
        File src = Environment.getExternalStoragePublicDirectory(source);
        if(!src.exists()){
            String name = FilenameUtils.getName(source);
            src = Environment.getExternalStoragePublicDirectory("DMS/tempMedia/"+name);
        }
        player.setSource(Uri.fromFile(src));
    }

    @Override
    public void onStarted(EasyVideoPlayer player) {
    }

    @Override
    public void onPaused(EasyVideoPlayer player) {

    }

    @Override
    public void onPreparing(EasyVideoPlayer player) {
        dialog = new MaterialDialog.Builder(VideoPlayer.this)
        .title(R.string.progress_wait)
        .content(R.string.please_wait)
        .progress(true,0)
        .build();
        dialog.show();
    }

    @Override
    public void onPrepared(EasyVideoPlayer player) {
        dialog.dismiss();
    }

    @Override
    public void onBuffering(int percent) {

    }

    @Override
    public void onError(EasyVideoPlayer player, Exception e) {

    }

    @Override
    public void onCompletion(EasyVideoPlayer player) {

    }

    @Override
    public void onRetry(EasyVideoPlayer player, Uri source) {

    }

    @Override
    public void onSubmit(EasyVideoPlayer player, Uri source) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        player.pause();
    }
}
