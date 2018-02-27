package com.disarm.surakshit.pdm.Chat.Holders;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.disarm.surakshit.pdm.Chat.Message;
import com.disarm.surakshit.pdm.R;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

/**
 * Created by naman on 27/2/18.
 */

public class OutgoingAudioHolders extends MessagesListAdapter.BaseMessageViewHolder<Message> {

    MediaPlayer mediaPlayer;
    SeekBar seekBar;
    ImageButton imageButton;
    public OutgoingAudioHolders(View itemView) {
        super(itemView);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(false);
        seekBar = (SeekBar) itemView.findViewById(R.id.incoming_seekBar);
        imageButton = (ImageButton) itemView.findViewById(R.id.incoming_imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    imageButton.setImageResource(android.R.drawable.ic_media_play);
                    mediaPlayer.pause();
                }
                else{
                    imageButton.setImageResource(android.R.drawable.ic_media_pause);
                    mediaPlayer.start();
                }
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                seekBar.setProgress(0);
                imageButton.setImageResource(android.R.drawable.ic_media_play);
                mediaPlayer.seekTo(0);
                mediaPlayer.pause();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mediaPlayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onBind(Message message) {
        try {
            mediaPlayer.setDataSource(message.getUrl());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    seekBar.setMax(mediaPlayer.getDuration());
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
        HandlerThread ht = new HandlerThread("Audio Thread");
        ht.start();
        Handler h = new Handler(ht.getLooper());
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer.isPlaying()){
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                }
            }
        };
    }
}
