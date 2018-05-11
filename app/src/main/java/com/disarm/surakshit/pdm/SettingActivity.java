package com.disarm.surakshit.pdm;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getSupportFragmentManager().getFragments().size()==0){
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingFragment())
                    .commit();
        }
    }


}
