package com.disarm.surakshit.pdm;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.disarm.surakshit.pdm.Chat.Utils.ChatUtils;
import com.disarm.surakshit.pdm.DB.DBEntities.App;
import com.disarm.surakshit.pdm.DB.DBEntities.Receiver;
import com.disarm.surakshit.pdm.DB.DBEntities.Receiver_;
import com.disarm.surakshit.pdm.DB.DBEntities.Sender;
import com.disarm.surakshit.pdm.DB.DBEntities.Sender_;
import com.disarm.surakshit.pdm.Encryption.KeyBasedFileProcessor;
import com.disarm.surakshit.pdm.Fragments.FragmentAdapter;
import com.disarm.surakshit.pdm.Util.DiffUtils;
import com.disarm.surakshit.pdm.Util.Params;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.osmdroid.bonuspack.kml.KmlDocument;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import io.objectbox.Box;

public class MainActivity extends AppCompatActivity {


    HandlerThread ht,ht_diff;
    Handler h,h_diff;
    static int total_kml=0;
    static HashSet<String> kmlFilesList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(fragmentAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        View root = tabLayout.getChildAt(0);
        if (root instanceof LinearLayout) {
            ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(getResources().getColor(R.color.white));
            drawable.setSize(2, 1);
            ((LinearLayout) root).setDividerPadding(10);
            ((LinearLayout) root).setDividerDrawable(drawable);
        }

        kmlFilesList = new HashSet<>();
        File kmlDir = Environment.getExternalStoragePublicDirectory("DMS/KML/Dest/SourceKml");
        for(File file : kmlDir.listFiles()){
            kmlFilesList.add(FilenameUtils.getBaseName(file.getName()));
            total_kml++;
        }
        ht = new HandlerThread("FileWatcher");
        ht.start();
        h = new Handler(ht.getLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                File kmlDir = Environment.getExternalStoragePublicDirectory("DMS/Working/SurakshitKml");
                File[] kmlfiles = kmlDir.listFiles();
                if(kmlfiles.length > total_kml){
                    for(File file : kmlfiles) {
                        if(!file.getName().contains(Params.SOURCE_PHONE_NO)){
                            continue;
                        }
                        if(!kmlFilesList.contains(FilenameUtils.getBaseName(file.getName()))){
                            boolean done = true;
                            String keyPath = Environment.getExternalStoragePublicDirectory("DMS/pgpPrivate/pri_"+ Params.SOURCE_PHONE_NO).getAbsolutePath();
                            try {
                                KeyBasedFileProcessor.decrypt(file.getAbsolutePath(),keyPath,Params.PASS_PHRASE);
                            } catch (Exception e) {
                                done = false;
                                e.printStackTrace();
                            }
                            if(done) {
                                total_kml++;
                                kmlFilesList.add(FilenameUtils.getBaseName(file.getName()));
                                try {
                                    updateDB(FilenameUtils.getBaseName(file.getName()));
                                } catch (IOException | ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                h.postDelayed(this,1000);
            }
        });
        ht_diff = new HandlerThread("diff");
        ht_diff.start();
        h_diff = new Handler(ht_diff.getLooper());
        h_diff.post(new Runnable() {
            @Override
            public void run() {
                File diffDir = Environment.getExternalStoragePublicDirectory("DMS/Working/SurakshitDiff");
                File latestDestDir = Environment.getExternalStoragePublicDirectory("DMS/KML/Dest/LatestKml");
                File sourceDestDir = Environment.getExternalStoragePublicDirectory("DMS/KML/Dest/SourceKml");
                File[] diffFiles = diffDir.listFiles();
                HashMap<String,File> myDiffFiles = new HashMap<>();
                for(File file : diffFiles){
                    String[] name = file.getName().split("_");
                    if(name[2].equals(Params.SOURCE_PHONE_NO)){
                        myDiffFiles.put(name[0],file);
                    }
                }

                HashMap<String,File> sourceFiles = new HashMap<>();
                for(File file : sourceDestDir.listFiles()){
                    String[] name = file.getName().split("_");
                    sourceFiles.put(name[0],file);
                }

                for(File file : latestDestDir.listFiles()){
                    String fileName = FilenameUtils.getBaseName(file.getName());
                    String split[] = fileName.split("_");
                    if(myDiffFiles.containsKey(split[0])){
                        File diff = myDiffFiles.get(split[0]);
                        String version = FilenameUtils.getBaseName(diff.getName()).split("_")[4];
                        if(!version.equals(split[4])){
                            File source = sourceFiles.get(split[0]);
                            if(DiffUtils.applyPatch(source,diff)){
                                try {
                                    FileUtils.forceDelete(file);
                                    final Box<Receiver> receiverBox = ((App)getApplication()).getBoxStore().boxFor(Receiver.class);
                                    final Box<Sender> senderBox = ((App)getApplication()).getBoxStore().boxFor(Sender.class);
                                    String number = split[1];

                                    List<Receiver> receivers = receiverBox.query().equal(Receiver_.number,number).build().find();
                                    List<Sender> senders = senderBox.query().equal(Sender_.number,number).build().find();

                                    Receiver receiver = receivers.get(0);
                                    File latestkml = Environment.getExternalStoragePublicDirectory("DMS/KML/Dest/LatestKml");
                                    for(File kml : latestkml.listFiles() ){
                                        if(kml.getName().contains(split[0])){
                                            KmlDocument latestkmldocument = new KmlDocument();
                                            latestkmldocument.parseKMLFile(kml);
                                            int total = Integer.parseInt(latestkmldocument.mKmlRoot.getExtendedData("total"));
                                            receiver.setUnread(total - receiver.getTotalMsg());
                                            receiver.setTotalMsg(total);
                                            String lastMsgReceiver = getLastMessage(latestkmldocument);
                                            receiver.setLastMessage(lastMsgReceiver);
                                            receiver.setKml(FileUtils.readFileToString(kml));
                                            if(senders.size()!=0){
                                                Sender sender = senders.get(0);
                                                String lastMsg = sender.getLastMessage();
                                                String sender_time = lastMsg.split("-")[0];
                                                DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
                                                Date sender_d = df.parse(sender_time);
                                                Date receiver_d = df.parse(lastMsgReceiver.split("-")[0]);
                                                if(sender_d.before(receiver_d)){
                                                    receiver.setLastUpdated(true);
                                                    sender.setLastUpdated(false);
                                                }
                                                else{
                                                    receiver.setLastUpdated(false);
                                                    sender.setLastUpdated(true);
                                                }
                                                senderBox.put(sender);
                                            }
                                            else{
                                                receiver.setLastUpdated(true);
                                            }
                                            receiverBox.put(receiver);
                                        }
                                    }
                                    senderBox.closeThreadResources();
                                    receiverBox.closeThreadResources();
                                } catch (IOException | ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                h_diff.postDelayed(this,1000);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ht.quit();
    }

    private void updateDB(String fileName) throws IOException, ParseException {
        File destKmlDir = Environment.getExternalStoragePublicDirectory("DMS/KML/Dest/SourceKml");
        for(File file : destKmlDir.listFiles()){
            if(file.getName().contains(FilenameUtils.getBaseName(fileName))){
                final Box<Receiver> receiverBox = ((App)getApplication()).getBoxStore().boxFor(Receiver.class);
                final Box<Sender> senderBox = ((App)getApplication()).getBoxStore().boxFor(Sender.class);
                Receiver receiver = new Receiver();
                String fileSplit[] = fileName.split("_");
                receiver.setNumber(fileSplit[1]);
                KmlDocument kmlDocument = new KmlDocument();
                kmlDocument.parseKMLFile(file);
                int total = Integer.parseInt(kmlDocument.mKmlRoot.getExtendedData("total"));
                receiver.setTotalMsg(total);
                String kml = FileUtils.readFileToString(file);
                receiver.setKml(kml);
                receiver.setUnread(0);
                String nextKey = "source";
                String msg = "";
                while(kmlDocument.mKmlRoot.mExtendedData.containsKey(nextKey)){
                    msg = kmlDocument.mKmlRoot.getExtendedData(nextKey);
                    nextKey = getTimeStampFromMsg(msg);
                }
                receiver.setLastMessage(msg);
                List<Sender> senderList = senderBox.query().contains(Sender_.number,fileSplit[1]).build().find();
                if(senderList.size()!=0){
                    Sender sender = senderList.get(0);
                    String message = sender.getLastMessage();
                    String split[] = message.split("-");
                    String time = split[0];
                    DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
                    Date d = df.parse(time);
                    String split2[] = msg.split("-");
                    String time2 = split2[0];
                    Date d2 = df.parse(time2);
                    if(d.after(d2)){
                        receiver.setLastUpdated(false);
                    }
                    else{
                        receiver.setLastUpdated(true);
                    }
                }
                else{
                    receiver.setLastUpdated(true);
                }
                receiverBox.put(receiver);
                receiverBox.closeThreadResources();
                senderBox.closeThreadResources();
            }
        }
    }

    private String getTimeStampFromMsg(String msg){
        Pattern p = Pattern.compile("-");
        String[] s = p.split(msg,4);
        return s[0];
    }

    private String getLastMessage(KmlDocument kml){
        String nextKey = "source";
        String msg = "";
        while (kml.mKmlRoot.mExtendedData.containsKey(nextKey)) {
            msg = kml.mKmlRoot.getExtendedData(nextKey);
            nextKey = getTimeStampFromMsg(msg);
        }
        return msg;
    }
}
