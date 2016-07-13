package com.disarm.sanna.pdm;

import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.disarm.sanna.pdm.BackgroundProcess.FileTask;
import com.disarm.sanna.pdm.Capture.AudioCapture;
import com.disarm.sanna.pdm.Capture.Photo;
import com.disarm.sanna.pdm.Capture.SmsCaptrue;
import com.disarm.sanna.pdm.Capture.Text;
import com.disarm.sanna.pdm.Capture.Video;
import com.disarm.sanna.pdm.Util.DividerItemDecoration;


/**
 * Created by Sanna on 21-06-2016.
 */
public class ActivityList extends AppCompatActivity implements View.OnClickListener {


    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    String type,ttlString,dest,latlong;
    Button submit,discard;
    EditText ttl,destination;
    public static int [] prgmImages={R.drawable.camera
                                    ,R.drawable.video_maker
                                    ,R.drawable.audiopocket
                                    ,R.drawable.textra_sms
                                    ,R.drawable.evolve_sms};
    public static String [] prgmNameList={"Photo","Video","Audio","Text","SMS"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_list);
        Intent myIntent = getIntent();
        type = myIntent.getStringExtra("IntentType");
        Log.v("type intent ",type);
        ttl = (EditText)findViewById(R.id.ttl);
        destination = (EditText)findViewById(R.id.destination);
        submit = (Button)findViewById(R.id.submit);
        discard = (Button)findViewById(R.id.discard);
        submit.setOnClickListener(this);
        discard.setOnClickListener(this);
        //startService(new Intent(this, LocationUpdateService.class));
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view_activitylist);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));


        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        //mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapterActivityList(prgmNameList,prgmImages);
        mRecyclerView.setAdapter(mAdapter);
        //prepareSituationData();
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new MainActivity.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                onclick(position);
                //Toast.makeText(ActivityList.this, "clicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }
    @Override
    public void onBackPressed() {
        ActivityList.this.finish();
        super.onBackPressed();
    }

    public void onclick(int i){
        switch (i){
            case 0:
                Intent intent=new Intent(getApplicationContext(), Photo.class);
                intent.putExtra("IntentType",type);
                startActivity(intent);
                break;
            case 1:
                Intent intent1=new Intent(getApplicationContext(), Video.class);
                intent1.putExtra("IntentType",type);
                startActivity(intent1);
                break;
            case 2:
                Intent intent2=new Intent(getApplicationContext(), AudioCapture.class);
                intent2.putExtra("IntentType",type);
                startActivity(intent2);
                break;
            case 3:
                showTextDialog();
                break;
            case 4:
                showSmsDialog();
                break;

        }

    }

    private void showTextDialog() {
        FragmentManager fm = getSupportFragmentManager();
        Text text= Text.newInstance("Add Text",type);
        text.show(fm, "activity_text");
    }

    private void showSmsDialog(){
        FragmentManager fm = getSupportFragmentManager();
        SmsCaptrue smsCaptrue = new SmsCaptrue();
        smsCaptrue.newInstance(type);
        smsCaptrue.show(fm,"activity_sms");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.submit:

                ttlString = ttl.getText().toString();
                dest = destination.getText().toString();
                if (ttlString.isEmpty()){
                    ttlString = "50";
                }
                if (dest.isEmpty()){
                    dest = "defaultMcs";
                }
                new FileTask().execute(ttlString,dest,latlong);

                ActivityList.this.finish();
                break;
            case R.id.discard:

                break;

        }
    }
    @Override
    protected void onDestroy() {
        //stopService(new Intent(this, LocationUpdateService.class));
        super.onDestroy();
    }
}
