package com.disarm.sanna.pdm;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by arka on 14/9/16.
 */
public class SelectCategoryActivity extends AppCompatActivity implements View.OnClickListener {
    Button categoryXender;
    Button categoryDisasterManagement;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_select_category);

        categoryXender = (Button)findViewById(R.id.b_category_xender);
        categoryDisasterManagement = (Button)findViewById(R.id.b_category_disaster_management);

        categoryXender.setOnClickListener(this);
        categoryDisasterManagement.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.b_category_xender:
                // Launch Social App
                break;
            case R.id.b_category_disaster_management:
                // Launch Disaster Management Activity
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
        }
    }

    /*
    Set background image to buttons
    */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        int buttonHeight,buttonWidth;
        Bitmap bitmap;
        Resources r = getResources();

        buttonHeight = categoryXender.getHeight();
        buttonWidth = categoryXender.getWidth();
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.file_share);
        categoryXender.setBackground(new BitmapDrawable(r, bitmap));


        buttonHeight = categoryDisasterManagement.getHeight();
        buttonWidth = categoryDisasterManagement.getWidth();
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.disaster_management);
        categoryDisasterManagement.setBackground(new BitmapDrawable(r, bitmap));
    }
}
