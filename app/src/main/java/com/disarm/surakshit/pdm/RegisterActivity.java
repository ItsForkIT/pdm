package com.disarm.surakshit.pdm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.disarm.surakshit.pdm.Util.Params;
import com.github.florent37.materialtextfield.MaterialTextField;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;

public class RegisterActivity extends AppCompatActivity {
    Button btn_volunteer,btn_manager;
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
        btn_volunteer = (Button) findViewById(R.id.btn_register_volunteer);
        btn_manager = (Button) findViewById(R.id.btn_register_manager);

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
        Button btn_submit = (Button) view.findViewById(R.id.btn_submit_password);
        Button btn_cancel = (Button) view.findViewById(R.id.btn_submit_cancel);
        final EditText pass = (EditText) view.findViewById(R.id.et_register_password);
        final MaterialTextField mtf = (MaterialTextField) view.findViewById(R.id.et_register_material);
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
}
