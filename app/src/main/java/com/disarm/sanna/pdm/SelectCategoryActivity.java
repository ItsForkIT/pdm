package com.disarm.sanna.pdm;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * Created by arka on 14/9/16.
 * Activity to choose between offline social sharing and disaster management category
 */
public class SelectCategoryActivity extends AppCompatActivity implements ImageButton.OnClickListener {
    Button categorySocialShare;
    Button categoryDisasterManagement;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_select_category);

        //categorySocialShare = (Button)findViewById(R.id.b_category_social_share);
        //categoryDisasterManagement = (Button)findViewById(R.id.b_category_disaster_management);
        ImageButton categorySocialShare = (ImageButton)findViewById(R.id.b_category_social_share);
        ImageButton categoryDisasterManagement = (ImageButton)findViewById(R.id.b_category_disaster_management);

        categorySocialShare.setOnClickListener(this);
        categoryDisasterManagement.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.b_category_social_share:
                // Launch Social App
                Intent intentSocialShare = new Intent(this, SocialShareActivity.class);
                startActivity(intentSocialShare);
                break;
            case R.id.b_category_disaster_management:
                // Launch Disaster Management Activity
                Intent intentDisasterManagement = new Intent(this, MainActivity.class);
                startActivity(intentDisasterManagement);
                break;
        }
    }

    /*
    Set background image to buttons
    */
   /* @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        int buttonHeight,buttonWidth;
        Bitmap bitmap;
        Resources r = getResources();

        buttonHeight = categorySocialShare.getHeight();
        buttonWidth = categorySocialShare.getWidth();
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.file_share);
        bitmap = Bitmap.createScaledBitmap(bitmap, buttonHeight, buttonWidth, true);
        categorySocialShare.setBackground(new BitmapDrawable(r, bitmap));

        buttonHeight = categoryDisasterManagement.getHeight();
        buttonWidth = categoryDisasterManagement.getWidth();
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.disaster_management);
        bitmap = Bitmap.createScaledBitmap(bitmap, buttonHeight, buttonWidth, true);
        categoryDisasterManagement.setBackground(new BitmapDrawable(r, bitmap));
    }*/
}
