package com.disarm.sanna.pdm.Capture;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.disarm.sanna.pdm.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.security.auth.Destroyable;

/**
 * Created by Sanna on 30-06-2016.
 */
public class Text extends DialogFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private Spinner variety,quantity;
    private Button save,back;
    static String type;
    EditText custom;
    StringBuilder msgString;
    String h,q,outputFile;
    static String root = Environment.getExternalStorageDirectory().toString();
    static String path =root + "/" + "DMS" + "/" + "tmp" + "/",group,groupID;
    private boolean c;

    public Text() {
        super();
    }
    public static Text newInstance(String title,String s){
        Text textFrag = new Text();
        Bundle args = new Bundle();
        args.putString("title", title);
        textFrag.setArguments(args);
        type = s;
        return textFrag;

    }
    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        variety = (Spinner)view.findViewById(R.id.spinner);
        quantity = (Spinner)view.findViewById(R.id.spinner2);
        save = (Button)view.findViewById(R.id.save);
        back = (Button)view.findViewById(R.id.back);
        custom = (EditText) view.findViewById(R.id.custom);
        custom.setVisibility(view.GONE);
        save.setOnClickListener(this);
        back.setOnClickListener(this);

        switch (type){
            case "Health":
                String[] healthArray = getResources().getStringArray(R.array.health_array);
                List<String> health = new ArrayList<String>(Arrays.asList(healthArray));
                ArrayAdapter<String> healthadapter = new ArrayAdapter<String>
                        (getActivity(), android.R.layout.simple_spinner_item, health);
                healthadapter.setDropDownViewResource
                        (android.R.layout.simple_spinner_dropdown_item);
                variety.setAdapter(healthadapter);
                variety.setOnItemSelectedListener(this);
                break;
            case "Food":
                String[] foodArray = getResources().getStringArray(R.array.food_array);
                List<String> food = new ArrayList<String>(Arrays.asList(foodArray));
                ArrayAdapter<String> foodadapter = new ArrayAdapter<String>
                        (getActivity(), android.R.layout.simple_spinner_item, food);
                foodadapter.setDropDownViewResource
                        (android.R.layout.simple_spinner_dropdown_item);
                variety.setAdapter(foodadapter);
                variety.setOnItemSelectedListener(this);
                break;
            case "Shelter":
                String[] shelterArray = getResources().getStringArray(R.array.shelter_array);
                List<String> shelter = new ArrayList<String>(Arrays.asList(shelterArray));
                ArrayAdapter<String> shelterAdapter = new ArrayAdapter<String>
                        (getActivity(), android.R.layout.simple_spinner_item, shelter);
                shelterAdapter.setDropDownViewResource
                        (android.R.layout.simple_spinner_dropdown_item);
                variety.setAdapter(shelterAdapter);
                variety.setOnItemSelectedListener(this);
                break;
            case "Victim":
                String[] victimArray = getResources().getStringArray(R.array.victim_array);
                List<String> victim = new ArrayList<String>(Arrays.asList(victimArray));
                ArrayAdapter<String> victimAdapter = new ArrayAdapter<String>
                        (getActivity(), android.R.layout.simple_spinner_item, victim);
                victimAdapter.setDropDownViewResource
                        (android.R.layout.simple_spinner_dropdown_item);
                variety.setAdapter(victimAdapter);
                variety.setOnItemSelectedListener(this);
                break;
        }
        String[] numArray = getResources().getStringArray(R.array.num_array);
        List<String> num = new ArrayList<String>(Arrays.asList(numArray));
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>
                (getActivity(), android.R.layout.simple_spinner_item, num);
        adapter1.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);
        quantity.setAdapter(adapter1);
        quantity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View v, int i, long l) {
                if (!adapterView.getItemAtPosition(i).toString().equals("Select")){
                    q = adapterView.getItemAtPosition(i).toString();
                }

                if (adapterView.getItemAtPosition(i).toString().equals("Custom")){
                    custom.setVisibility(v.getVisibility());
                    custom.setEnabled(true) ;
                    c = true;
                }else {
                    custom.setEnabled(false);
                    c = false;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_text, container);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.save:
                if (c){
                    q = custom.getText().toString();
                }

                msgString = new StringBuilder(type+": "+ h + q);

                    outputFile = getFilename();
                    File file = new File(outputFile);
                    // If file does not exists, then create it
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        FileWriter fw = new FileWriter(file.getAbsoluteFile());
                        BufferedWriter bw = new BufferedWriter(fw);
                        Log.v("File","Writing "+msgString.toString());
                        bw.write(msgString.toString());
                        bw.flush();
                        bw.close();
                        Log.v("FIle","written");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getActivity(), "File Successfully Saved", Toast.LENGTH_SHORT).show();


                Log.v("final ", msgString.toString());
                getDialog().dismiss();
                break;
            case R.id.back:
                getDialog().dismiss();
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (type){
            case "Health":
                if (!adapterView.getItemAtPosition(i).toString().equals("Select")) {
                    h = adapterView.getItemAtPosition(i).toString() + ": ";
                }
                break;
            case "Food":
                if (!adapterView.getItemAtPosition(i).toString().equals("Select")) {
                    h = adapterView.getItemAtPosition(i).toString() + ": ";
                }
                break;
            case "Shelter":
                if (!adapterView.getItemAtPosition(i).toString().equals("Select")) {
                    h = adapterView.getItemAtPosition(i).toString() + ": ";
                }
                break;
            case "Victim":
                if (!adapterView.getItemAtPosition(i).toString().equals("Select")) {
                    h = adapterView.getItemAtPosition(i).toString() + ": ";
                }
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    private String getFilename() {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        group = type;
        groupID = "1";
        return (path + "TXT_" +  group + "_" + timeStamp + "_" + groupID + ".txt");
    }
}
