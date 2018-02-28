package com.disarm.surakshit.pdm.Chat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.alexvasilkov.gestures.views.GestureImageView;
import com.disarm.surakshit.pdm.R;
import com.squareup.picasso.Picasso;

import java.io.File;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        GestureImageView image  = (GestureImageView) findViewById(R.id.imageview);
        String path = getIntent().getStringExtra("url");
        File f = Environment.getExternalStoragePublicDirectory(path);
        Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        if(height > 2450)
            height = 2450;
        if(width > 2450)
            width = 2450;
        Picasso.with(this).load(f).resize(width,height).into(image);
    }
}
