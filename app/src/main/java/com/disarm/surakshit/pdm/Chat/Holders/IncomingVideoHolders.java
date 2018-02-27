package com.disarm.surakshit.pdm.Chat.Holders;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import com.disarm.surakshit.pdm.Chat.Message;
import com.disarm.surakshit.pdm.Chat.VideoPlayer;
import com.disarm.surakshit.pdm.R;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.io.File;

/**
 * Created by naman on 27/2/18.
 */

public class IncomingVideoHolders extends MessagesListAdapter.BaseMessageViewHolder<Message> {

    ImageView preview,playbtn;
    Context context;
    public IncomingVideoHolders(View itemView) {
        super(itemView);
        context = itemView.getContext();
        preview = (ImageView) itemView.findViewById(R.id.incoming_video_preview);
        playbtn = (ImageView) itemView.findViewById(R.id.incoming_video_play);
    }

    @Override
    public void onBind(final Message message) {
        File f = Environment.getExternalStoragePublicDirectory(message.getUrl());
        Bitmap bmp = ThumbnailUtils.createVideoThumbnail(f.getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
        preview.setImageBitmap(bmp);
        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, VideoPlayer.class);
                i.putExtra("url",message.getUrl());
                context.startActivity(i);
            }
        });
    }
}
