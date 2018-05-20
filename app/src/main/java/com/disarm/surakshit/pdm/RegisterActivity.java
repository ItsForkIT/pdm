package com.disarm.surakshit.pdm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.disarm.surakshit.pdm.Encryption.RSAKeyPairGenerator;
import com.disarm.surakshit.pdm.Util.Params;
import com.github.florent37.materialtextfield.MaterialTextField;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;

import java.io.File;

public class RegisterActivity extends AppCompatActivity {
    Button btn_volunteer,btn_manager,btn_user;
    SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("Surakshit",MODE_PRIVATE);
        if(sp.getString("user",null)!=null){
            Params.WHO = sp.getString("user",null);
            Intent i = new Intent(RegisterActivity.this,WriteSettingActivity.class);
            startActivity(i);
            finish();
        }
        setContentView(R.layout.activity_register);
        btn_volunteer = findViewById(R.id.btn_register_volunteer);
        btn_manager = findViewById(R.id.btn_register_manager);
        btn_user = findViewById(R.id.btn_register_normal);
        btn_volunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog("volunteer");
            }
        });

        btn_manager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog("manager");
            }
        });

        btn_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateKey();
                SharedPreferences.Editor edit = sp.edit();
                Params.WHO = "normal";
                edit.putString("user","normal");
                edit.apply();
                Intent i = new Intent(RegisterActivity.this,WriteSettingActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    private void createDialog(final String who){
        View view = getLayoutInflater().inflate(R.layout.dialog_register,null);
        final MaterialStyledDialog dialog = new MaterialStyledDialog.Builder(this)
                .setTitle(R.string.input_password)
                .setStyle(Style.HEADER_WITH_TITLE)
                .withDarkerOverlay(true)
                .withDialogAnimation(true)
                .setCancelable(false)
                .setCustomView(view,10,20,10,20)
                .build();
        dialog.show();
        Button btn_submit = view.findViewById(R.id.btn_submit_password);
        Button btn_cancel = view.findViewById(R.id.btn_submit_cancel);
        final EditText pass = view.findViewById(R.id.et_register_password);
        final MaterialTextField mtf = view.findViewById(R.id.et_register_material);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = "";
                if(who.equals("volunteer")){
                    password = "volunteer@disarm321";
                }
                else{
                    password = "manager@disarm123";
                }
                if(pass.getText().toString().equals(password)){
                    SharedPreferences.Editor edit = sp.edit();
                    edit.putString("user",who);
                    edit.apply();
                    dialog.dismiss();
                    Params.WHO = who;
                    generateKey();
                    Intent i = new Intent(RegisterActivity.this,WriteSettingActivity.class);
                    startActivity(i);
                    finish();
                }
                else{
                    Toast.makeText(RegisterActivity.this,"Wrong Password",Toast.LENGTH_SHORT).show();
                    mtf.startAnimation(AnimationUtils.loadAnimation(RegisterActivity.this,R.anim.shake));
                    pass.setText("");
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void generateKey(){
        File dir = Environment.getExternalStoragePublicDirectory("DMS/pgpPrivate/");
        if(!dir.exists()) {
            dir.mkdir();
        }
        try {
                RSAKeyPairGenerator.generate(Params.SOURCE_PHONE_NO, Params.PASS_PHRASE, Environment.getExternalStorageDirectory().getPath()+"/DMS/pgpPrivate/pri_"+Params.SOURCE_PHONE_NO+".bgp",Environment.getExternalStorageDirectory().getPath()+"/DMS/Working/pgpKey/pub_"+Params.SOURCE_PHONE_NO+".bgp");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Error",e.toString());
            }
    }
}
