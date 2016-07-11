package com.disarm.sanna.pdm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.flaviofaria.kenburnsview.KenBurnsView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by disarm on 11/7/16.
 */
public class SplashActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File dir = Environment.getExternalStoragePublicDirectory("DMS/");
        if (!dir.exists()) {
            dir.mkdir();
        }
        final File configFile = new File(dir,"source.txt");

        if (configFile.exists())  {
            // TODO Auto-generated method stub
            Intent iinent = new Intent(this, MainActivity.class);
            startActivity(iinent);
            finish();

        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        final Button submitButton = (Button) findViewById(R.id.submitButton);


        submitButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                final EditText phoneText1  = (EditText) findViewById(R.id.phoneText);
                final String phoneTextVal = phoneText1.getText().toString();

                if(phoneTextVal.length() == 10 && phoneTextVal.matches("^[789]\\d{9}$")) {
                    if (!configFile.exists())  {
                        try  {
                            Log.d("Config File created ", " Config File created ");
                            configFile.createNewFile();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    try {


                        BufferedWriter buf = new BufferedWriter(new FileWriter(configFile, true));
                        buf.write(phoneTextVal);
                        buf.flush();
                        buf.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    // TODO Auto-generated method stub
                    Intent iinent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(iinent);
                    finish();
                }
                else
                {
                    phoneText1.setError("Enter Valid No.");
                }

            }
        });



    }
}
