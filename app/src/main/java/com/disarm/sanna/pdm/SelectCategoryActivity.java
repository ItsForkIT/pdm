package com.disarm.sanna.pdm;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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
                Intent intentDisasterManagement = new Intent(this, SurakshitActivity.class);
                startActivity(intentDisasterManagement);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage("Exit Application")
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                                    System.exit(0);
                            }
                        });
        alertDialogBuilder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}
