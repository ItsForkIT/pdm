package com.disarm.sanna.pdm;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.disarm.sanna.pdm.Util.CopyAssets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by disarm on 11/7/16.
 */
public class SplashActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PERMISSION_ALL = 1;
    public File dmsFolder = Environment.getExternalStoragePublicDirectory("DMS/");
    public File workingFolder = Environment.getExternalStoragePublicDirectory("DMS/Working");
    public File tmpFolder = Environment.getExternalStoragePublicDirectory("DMS/tmp");
    static String root = Environment.getExternalStorageDirectory().toString();
    final static String TARGET_MAP_PATH = root + "/DMS/Map/";
    final static String TARGET_DMS_PATH = root + "/DMS/";
    final File configFile = new File(dmsFolder,"source.txt");
    private ProgressDialog progress;
    private EditText phoneText1;
    private Button submitButton;
    String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE};
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        submitButton = (Button) findViewById(R.id.submitButton);
        phoneText1 = (EditText) findViewById(R.id.phoneText);
        submitButton.setVisibility(View.GONE);
        phoneText1.setVisibility(View.GONE);
        phoneText1.setCursorVisible(false);
        progress=new ProgressDialog(this);
        progress.setMessage("Checking All Configuration");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();

        if (!checkPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }


        submitButton.setOnClickListener( this);
    }
    private  void afterPermissionExecute(){
        if (!dmsFolder.exists() && !workingFolder.exists() && !tmpFolder.exists()) {
            Log.v("File", "creating files");
            dmsFolder.mkdir();
            workingFolder.mkdir();
            tmpFolder.mkdir();
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("firstTime", false)) {
            // <---- run your one time code here
            // Copy files from assets folder

            CopyAssets copy = new CopyAssets(this);
            copy.copyFileOrDir("");

            // mark first time has runned.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.commit();
        }
        if (checkAllFolder()){

            progress.setMessage("All Folder : OK");
        }

        if (checkSourceFile()){
            progress.setMessage("Source : OK");
            progress.setMessage("Asset : OK");

            progress.dismiss();
            callWriteSettingActivity();
        }else {
            phoneText1.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.VISIBLE);
            progress.dismiss();
        }

    }
    private boolean checkAllFolder() {
        boolean isAllFolderExit = false;
        if (!dmsFolder.exists() && !workingFolder.exists() && !tmpFolder.exists()) {
            Log.v("File","creating files");
            dmsFolder.mkdir();
            workingFolder.mkdir();
            tmpFolder.mkdir();
            isAllFolderExit = true;
        }
        return isAllFolderExit;
    }

    private boolean checkSourceFile() {
        boolean isSourceExit = false;
        if (configFile.exists()) {
            // TODO Auto-generated method stub
            isSourceExit = true;
        }
        return isSourceExit;
    }

    private void callWriteSettingActivity(){
        Intent iinent = new Intent(this,WriteSettingActivity.class);
        startActivity(iinent);
        finish();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.submitButton){
            final String phoneTextVal = phoneText1.getText().toString();

            if(phoneTextVal.length() == 10 && phoneTextVal.matches("^[789]\\d{9}$")) {
                if (!configFile.exists())  {
                    try  {
                        Log.d("source File created ", " source File created ");
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
                Intent inent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(inent);
                finish();
            }
            else
            {
                phoneText1.setError("Enter Valid No.");
            }
        }
    }

    private boolean checkPermissions(Context context, String[] permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void showRequestPermissionWriteSettings() {
        boolean hasSelfPermission = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hasSelfPermission = Settings.System.canWrite(this);
        }
        if (hasSelfPermission) {

        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent,1);

        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //showRequestPermissionWriteSettings();
                    afterPermissionExecute();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
