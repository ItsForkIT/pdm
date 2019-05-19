package com.disarm.surakshit.pdm;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.disarm.surakshit.pdm.Chat.ChatActivity;
import com.disarm.surakshit.pdm.DB.DBEntities.App;
import com.disarm.surakshit.pdm.DB.DBEntities.Receiver;
import com.disarm.surakshit.pdm.DB.DBEntities.Receiver_;
import com.disarm.surakshit.pdm.DB.DBEntities.Sender;
import com.disarm.surakshit.pdm.DB.DBEntities.Sender_;

import com.disarm.surakshit.pdm.Encryption.KeyBasedFileProcessor;
import com.disarm.surakshit.pdm.Fragments.ChatFragment;
import com.disarm.surakshit.pdm.Fragments.FragmentAdapter;
import com.disarm.surakshit.pdm.Fragments.MapFragment;
import com.disarm.surakshit.pdm.Fragments.MergedMapFragment;
import com.disarm.surakshit.pdm.P2PConnect.ApManager;
import com.disarm.surakshit.pdm.Service.P2PConnectService;
import com.disarm.surakshit.pdm.Service.SyncService;
import com.disarm.surakshit.pdm.Util.DiffUtils;
import com.disarm.surakshit.pdm.Util.Params;
import com.disarm.surakshit.pdm.location.LocationState;
import com.disarm.surakshit.pdm.location.MLocation;
import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.core.ContactPickerActivity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.osmdroid.bonuspack.kml.KmlDocument;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import io.objectbox.Box;

public class MainActivity extends AppCompatActivity {

    HandlerThread ht;
    Handler h, h_diff;
    static int total_kml = 0;
    static HashSet<String> kmlFilesList;
    private boolean gpsService = false;
    public static SyncService syncService;
    public static P2PConnectService myService;
    public static boolean syncServiceBound = false;
    public static boolean myServiceBound = false;
    LocationManager lm;
    LocationListener locationListener;
    WifiManager wifimanager;
    //TODO: unable to start BroadcastListActivity
    //TODO: unable to start ChatActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(new ChatFragment(), "Chat");
        adapter.addFragment(new MapFragment(), "Map");
        adapter.addFragment(new MergedMapFragment(), "Merged Map");
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        crashLog();
        wifimanager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        startService();
        kmlFilesList = new HashSet<>();
        File kmlDir = Environment.getExternalStoragePublicDirectory("DMS/KML/Dest/SourceKml");
        for (File file : kmlDir.listFiles()) {
            if (FilenameUtils.getExtension(file.getName()).equals("kml")) {
                kmlFilesList.add(FilenameUtils.getBaseName(file.getName()));
                total_kml++;
            }
        }
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        ht = new HandlerThread("FileWatcher");
        ht.start();
        h = new Handler(ht.getLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                File kmlDir = Environment.getExternalStoragePublicDirectory("DMS/Working/SurakshitKml");
                File[] kmlfiles = kmlDir.listFiles();
                if (kmlfiles == null) {
                    Log.d("Main_Activity", "S_KML skipping... ");
                    return;
                }
                if (kmlfiles.length > total_kml) {
                    for (File file : kmlfiles) {
                        String name = FilenameUtils.getBaseName(file.getName());
                        String source = name.split("_")[1];
                        if (source.contains(Params.SOURCE_PHONE_NO))
                            continue;
                        try {
                            if (!(name.contains(Params.SOURCE_PHONE_NO) || (name.contains("user") && !name.contains(Params.SOURCE_PHONE_NO)) || (Params.WHO.equalsIgnoreCase("volunteer") && name.contains("volunteer") && !name.contains(Params.SOURCE_PHONE_NO)))) {
                                Log.d("SIGNED", "Skipping " + name);
                                continue;
                            } else {
                                Log.d("SIGNED", "Found file" + name);
                            }
                        } catch (Exception e) {
                            continue;
                        }
                        if (!kmlFilesList.contains(name)) {
                            boolean done = true, isVolunteer = false;
                            String keyPath;
                            if (name.contains("user")) {
                                keyPath = Environment.getExternalStoragePublicDirectory("DMS/Working/pgpKey/pri_user.bgp").getAbsolutePath();
                            } else if (name.contains("volunteer")) {
                                isVolunteer = true;
                                keyPath = Environment.getExternalStoragePublicDirectory("DMS/Working/pgpKey/pri_volunteer.bgp").getAbsolutePath();
                            } else {
                                keyPath = Environment.getExternalStoragePublicDirectory("DMS/pgpPrivate/pri_" + Params.SOURCE_PHONE_NO + ".bgp").getAbsolutePath();
                            }
                            try {
                                if (!isVolunteer)
                                    KeyBasedFileProcessor.decrypt(file.getAbsolutePath(), keyPath, Params.PASS_PHRASE);
                                else {
                                    KeyBasedFileProcessor.decrypt(file.getAbsolutePath(), keyPath, "volunteer@disarm321");
                                }
                            } catch (Exception e) {
                                done = false;
                                Log.d("SIGNED", e.getMessage());
                                e.printStackTrace();

                            }
                            if (done) {
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
                h.postDelayed(this, 1500);
            }
        });
        h_diff = new Handler(ht.getLooper());
        h_diff.post(new Runnable() {
            @Override
            public void run() {
                File diffDir = Environment.getExternalStoragePublicDirectory("DMS/Working/SurakshitDiff");
                File latestDestDir = Environment.getExternalStoragePublicDirectory("DMS/KML/Dest/LatestKml");
                File sourceDestDir = Environment.getExternalStoragePublicDirectory("DMS/KML/Dest/SourceKml");
                File[] diffFiles = diffDir.listFiles();
                HashMap<String, File> myDiffFiles = new HashMap<>();
                HashMap<String, Integer> latestVersion = new HashMap<>();
                if (diffFiles == null) {
                    Log.d("Main_Activity", "Diff Dir Files skipping... ");
                    return;
                }
                for (File file : diffFiles) {
                    String[] name = FilenameUtils.getBaseName(file.getName()).split("_");
                    if (!latestVersion.containsKey(name[0])) {
                        latestVersion.put(name[0], Integer.parseInt(name[4]));
                    }
                    if ((name[2].contains(Params.SOURCE_PHONE_NO) || name[2].contains("user") || name[2].contains("volunteer")) && Integer.parseInt(name[4]) >= latestVersion.get(name[0])) {
                        myDiffFiles.put(name[0], file);
                        Log.d("DIFF TEST", "Adding files in diff : " + file.getName());
                        latestVersion.put(name[0], Integer.parseInt(name[4]));
                    }
                }
                File[] sourceDestDirFiles = sourceDestDir.listFiles();
                File[] latestDestDirFiles = latestDestDir.listFiles();
                if (sourceDestDirFiles == null) {
                    Log.d("Main_Activity", "Dest Source Files skipping... ");
                    return;
                }
                if (latestDestDirFiles == null) {
                    Log.d("Main_Activity", "Dest Latest Files skipping... ");
                    return;
                }
                HashMap<String, File> sourceFiles = new HashMap<>();
                for (File file : sourceDestDirFiles) {
                    if (FilenameUtils.getExtension(file.getName()).equals("kml")) {
                        String[] name = file.getName().split("_");
                        sourceFiles.put(name[0], file);
                    }
                }
                if (latestDestDirFiles.length > 0) {
                    Log.d("DIFF TEST", "Inside more than 0 files");
                    for (File file : latestDestDirFiles) {
                        if (FilenameUtils.getExtension(file.getName()).equals("kml")) {
                            String fileName = FilenameUtils.getBaseName(file.getName());
                            String split[] = fileName.split("_");
                            if (myDiffFiles.containsKey(split[0])) {
                                Log.d("DIFF TEST", "Found Diff Files");
                                File diff = myDiffFiles.get(split[0]);
                                int version = Integer.parseInt(FilenameUtils.getBaseName(diff.getName()).split("_")[4]);
                                int curr_version = Integer.parseInt(split[4]);
                                if (curr_version < version) {
                                    Log.d("DIFF TEST", "Found Latest version Diff Files");
                                    File source = sourceFiles.get(split[0]);
                                    if (DiffUtils.applyPatch(source, diff, getApplication(), MainActivity.this)) {
                                        Log.d("DIFF TEST", "DIFF APPLIED");
                                        try {
                                            FileUtils.forceDelete(file);
                                            final Box<Receiver> receiverBox = ((App) getApplication()).getBoxStore().boxFor(Receiver.class);
                                            final Box<Sender> senderBox = ((App) getApplication()).getBoxStore().boxFor(Sender.class);
                                            String number = split[1];
                                            boolean isVolunteer = false, isUser = false;
                                            if (fileName.contains("volunteer")) {
                                                isVolunteer = true;
                                            } else if (fileName.contains("user")) {
                                                isUser = true;
                                            }
                                            List<Receiver> receivers = receiverBox.query().equal(Receiver_.number, number)
                                                    .equal(Receiver_.forVolunteer, isVolunteer)
                                                    .equal(Receiver_.forUser, isUser).build().find();
                                            List<Sender> senders = senderBox.query().equal(Sender_.number, number)
                                                    .equal(Sender_.forVolunteer, isVolunteer)
                                                    .equal(Sender_.forUser, isUser).build().find();

                                            Receiver receiver = receivers.get(0);
                                            File latestkml = Environment.getExternalStoragePublicDirectory("DMS/KML/Dest/LatestKml");
                                            if (latestkml.listFiles() == null) {
                                                Log.d("Main_Activity", "Latest KML Files skipping... ");
                                                return;
                                            }
                                            for (File kml : latestkml.listFiles()) {
                                                if (FilenameUtils.getExtension(file.getName()).equals("kml")) {
                                                    if (kml.getName().contains(split[0])) {
                                                        KmlDocument latestkmldocument = new KmlDocument();
                                                        latestkmldocument.parseKMLFile(kml);
                                                        int total = Integer.parseInt(latestkmldocument.mKmlRoot.getExtendedData("total"));
                                                        receiver.setUnread(total - receiver.getTotalMsg());
                                                        receiver.setTotalMsg(total);
                                                        String lastMsgReceiver = getLastMessage(latestkmldocument);
                                                        receiver.setLastMessage(lastMsgReceiver);
                                                        receiver.setKml(FileUtils.readFileToString(kml));
                                                        if (senders.size() != 0) {
                                                            Sender sender = senders.get(0);
                                                            String lastMsg = sender.getLastMessage();
                                                            String sender_time = lastMsg.split("-")[0];
                                                            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
                                                            Date sender_d = df.parse(sender_time);
                                                            Date receiver_d = df.parse(lastMsgReceiver.split("-")[0]);
                                                            if (sender_d.before(receiver_d)) {
                                                                receiver.setLastUpdated(true);
                                                                sender.setLastUpdated(false);
                                                            } else {
                                                                receiver.setLastUpdated(false);
                                                                sender.setLastUpdated(true);
                                                            }
                                                            senderBox.put(sender);
                                                        } else {
                                                            receiver.setLastUpdated(true);
                                                        }
                                                        receiverBox.put(receiver);
                                                    }
                                                }
                                            }
                                            senderBox.closeThreadResources();
                                            receiverBox.closeThreadResources();
                                        } catch (IOException | ParseException e) {
                                            Log.d("DIFF TEST", "Diff Failed");
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    for (File diff : diffFiles) {
                        String split[] = diff.getName().split("_");
                        if (split[1].equals(Params.SOURCE_PHONE_NO)) {
                            continue;
                        }
                        String identifier = split[0];
                        File source = sourceFiles.get(identifier);
                        if (DiffUtils.applyPatch(source, diff, getApplication(), MainActivity.this)) {
                            final Box<Receiver> receiverBox = ((App) getApplication()).getBoxStore().boxFor(Receiver.class);
                            final Box<Sender> senderBox = ((App) getApplication()).getBoxStore().boxFor(Sender.class);
                            String number = split[1];
                            boolean isVolunteer = false, isUser = false;
                            if (diff.getName().contains("volunteer")) {
                                isVolunteer = true;
                            } else if (diff.getName().contains("user")) {
                                isUser = true;
                            }
                            List<Receiver> receivers = receiverBox.query().equal(Receiver_.number, number)
                                    .equal(Receiver_.forVolunteer, isVolunteer)
                                    .equal(Receiver_.forUser, isUser).build().find();
                            List<Sender> senders = senderBox.query().equal(Sender_.number, number)
                                    .equal(Sender_.forVolunteer, isVolunteer)
                                    .equal(Sender_.forUser, isUser).build().find();

                            Receiver receiver = receivers.get(0);
                            File latestkml = Environment.getExternalStoragePublicDirectory("DMS/KML/Dest/LatestKml");
                            if (latestkml.listFiles() == null) {
                                Log.d("Main_Activity", "Latest KML Files skipping... ");
                                return;
                            }
                            for (File kml : latestkml.listFiles()) {
                                if (FilenameUtils.getExtension(kml.getName()).equals("kml")) {
                                    if (kml.getName().contains(identifier)) {
                                        KmlDocument latestkmldocument = new KmlDocument();
                                        latestkmldocument.parseKMLFile(kml);
                                        int total = Integer.parseInt(latestkmldocument.mKmlRoot.getExtendedData("total"));
                                        receiver.setUnread(total - receiver.getTotalMsg());
                                        receiver.setTotalMsg(total);
                                        String lastMsgReceiver = getLastMessage(latestkmldocument);
                                        receiver.setLastMessage(lastMsgReceiver);
                                        try {
                                            receiver.setKml(FileUtils.readFileToString(kml));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        if (senders.size() != 0) {
                                            Sender sender = senders.get(0);
                                            String lastMsg = sender.getLastMessage();
                                            String sender_time = lastMsg.split("-")[0];
                                            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
                                            Date sender_d = null;
                                            try {
                                                sender_d = df.parse(sender_time);
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                            Date receiver_d = null;
                                            try {
                                                receiver_d = df.parse(lastMsgReceiver.split("-")[0]);
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                            assert sender_d != null;
                                            if (sender_d.before(receiver_d)) {
                                                receiver.setLastUpdated(true);
                                                sender.setLastUpdated(false);
                                            } else {
                                                receiver.setLastUpdated(false);
                                                sender.setLastUpdated(true);
                                            }
                                            senderBox.put(sender);
                                        } else {
                                            receiver.setLastUpdated(true);
                                        }
                                        receiverBox.put(receiver);
                                    }
                                }
                            }
                            senderBox.closeThreadResources();
                            receiverBox.closeThreadResources();
                        }
                    }
                }

                h_diff.postDelayed(this, 1500);
            }
        });
        final Handler h_tempKey = new Handler(ht.getLooper());
        h_tempKey.post(new Runnable() {
            @Override
            public void run() {
                File tempDir = Environment.getExternalStoragePublicDirectory("DMS/temp");
                File pgpKeyDir = Environment.getExternalStoragePublicDirectory("DMS/Working/pgpKey");
                HashMap<String, File> keyFile = new HashMap<>();
                File[] pgpKeyDirFiles = pgpKeyDir.listFiles();
                File[] tempDirFiles = tempDir.listFiles();
                if (pgpKeyDirFiles == null) {
                    Log.d("Main_Activity", "PGP Files skipping... ");
                    return;
                }
                if (tempDirFiles == null) {
                    Log.d("Main_Activity", "temp Files skipping... ");
                    return;
                }
                for (File file : pgpKeyDirFiles) {
                    if (FilenameUtils.getBaseName(file.getName()).contains("_") && FilenameUtils.getExtension(file.getName()).equals("bgp")) {
                        String number = FilenameUtils.getBaseName(file.getName()).split("_")[1];
                        keyFile.put(number, file);
                    }
                }

                for (File file : tempDirFiles) {
                    String fileName[] = FilenameUtils.getBaseName(file.getName()).split("_");
                    String fileNumber = fileName[2];
                    String unique = fileName[0];
                    if (keyFile.containsKey(fileNumber)) {
                        String inputPath = file.getAbsolutePath();
                        String publicKeyPath = Environment.getExternalStoragePublicDirectory("DMS/Working/pgpKey/pub_" + fileNumber + ".bgp").getAbsolutePath();
                        String outputFilePath = Environment.getExternalStoragePublicDirectory("DMS/Working/SurakshitKml/"
                                + FilenameUtils.getBaseName(file.getName()) + ".bgp")
                                .getAbsolutePath();
                        try {
                            KeyBasedFileProcessor.encrypt(inputPath, publicKeyPath, outputFilePath);
                            String outputPath = "DMS/KML/Source/LatestKml/";
                            String outputPath2 = "DMS/KML/Source/SourceKml/";
                            File file1 = Environment.getExternalStoragePublicDirectory(outputPath + file.getName());
                            File file2 = Environment.getExternalStoragePublicDirectory(outputPath2 + file.getName());
                            FileUtils.copyFile(file, file1);
                            FileUtils.copyFile(file, file2);
                            FileUtils.forceDelete(file);
                            File tempMedia = Environment.getExternalStoragePublicDirectory("DMS/tempMedia");
                            for (File f : tempMedia.listFiles()) {
                                String type = "";
                                if (f.getName().contains(unique)) {
                                    switch (FilenameUtils.getExtension(f.getName())) {
                                        case "jpeg":
                                            type = "Images";
                                            break;
                                        case "mp4":
                                            type = "Videos";
                                            break;
                                        case "png":
                                            type = "Map";
                                            break;
                                        case "3gp":
                                            type = "Audio";
                                            break;
                                    }
                                    String outputFolder = "DMS/Working/Surakshit" + type + "/";
                                    File outputFile = Environment.getExternalStoragePublicDirectory(outputFolder + f.getName());
                                    FileUtils.moveFile(f, outputFile);
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                h_tempKey.postDelayed(this, 1000);
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
            AlertDialog ad = new AlertDialog.Builder(this)
                    .setTitle("WARNING")
                    .setMessage("Changing any setting will lead you to restart of the app")
                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent ii = new Intent(MainActivity.this, SettingActivity.class);
                            startActivity(ii);
                            dialogInterface.dismiss();
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create();
            ad.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ht.quit();
        unbindAllService();
        boolean isHotspotOn = ApManager.isApOn(this);
        if(isHotspotOn){
            ApManager.configApState(this);
            wifimanager.setWifiEnabled(true);
            Log.d("XOB","Stopping XOB");
        }
    }

    private void updateDB(String fileName) throws IOException, ParseException {
        File destKmlDir = Environment.getExternalStoragePublicDirectory("DMS/KML/Dest/SourceKml");
        if (destKmlDir.listFiles() == null) {
            Log.d("Main_Activity", "Source KML Files skipping... ");
            return;
        }
        for (File file : destKmlDir.listFiles()) {
            if (file.getName().contains(FilenameUtils.getBaseName(fileName)) && FilenameUtils.getExtension(file.getName()).equals("kml")) {
                final Box<Receiver> receiverBox = ((App) getApplication()).getBoxStore().boxFor(Receiver.class);
                final Box<Sender> senderBox = ((App) getApplication()).getBoxStore().boxFor(Sender.class);
                Receiver receiver = new Receiver();
                String fileSplit[] = fileName.split("_");
                receiver.setNumber(fileSplit[1]);
                if (fileSplit[2].equalsIgnoreCase("volunteer")) {
                    receiver.setForVolunteer(true);
                    receiver.setForUser(false);
                    Log.d("FFFFFF", "volunteer files");
                } else if (fileSplit[2].equalsIgnoreCase("user")) {
                    receiver.setForUser(true);
                    receiver.setForVolunteer(false);
                    Log.d("FFFFFF", "user files");
                } else {
                    receiver.setForVolunteer(false);
                    receiver.setForUser(false);
                }
                KmlDocument kmlDocument = new KmlDocument();
                kmlDocument.parseKMLFile(file);
                int total = Integer.parseInt(kmlDocument.mKmlRoot.getExtendedData("total"));
                receiver.setTotalMsg(total);
                String kml = FileUtils.readFileToString(file);
                receiver.setKml(kml);
                receiver.setUnread(0);
                String nextKey = "source";
                String msg = "";
                while (kmlDocument.mKmlRoot.mExtendedData.containsKey(nextKey)) {
                    msg = kmlDocument.mKmlRoot.getExtendedData(nextKey);
                    nextKey = getTimeStampFromMsg(msg);
                }
                receiver.setLastMessage(msg);
                List<Sender> senderList = senderBox.query().contains(Sender_.number, fileSplit[1]).build().find();
                if (senderList.size() != 0) {
                    Sender sender = senderList.get(0);
                    String message = sender.getLastMessage();
                    String split[] = message.split("-");
                    String time = split[0];
                    DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
                    Date d = df.parse(time);
                    String split2[] = msg.split("-");
                    String time2 = split2[0];
                    Date d2 = df.parse(time2);
                    if (d.after(d2)) {
                        receiver.setLastUpdated(false);
                    } else {
                        receiver.setLastUpdated(true);
                    }
                } else {
                    receiver.setLastUpdated(true);
                }
                receiverBox.put(receiver);
                receiverBox.closeThreadResources();
                senderBox.closeThreadResources();
            }
        }
    }

    private String getTimeStampFromMsg(String msg) {
        Pattern p = Pattern.compile("-");
        String[] s = p.split(msg, 4);
        return s[0];
    }

    private String getLastMessage(KmlDocument kml) {
        String nextKey = "source";
        String msg = "";
        while (kml.mKmlRoot.mExtendedData.containsKey(nextKey)) {
            msg = kml.mKmlRoot.getExtendedData(nextKey);
            nextKey = getTimeStampFromMsg(msg);
        }
        return msg;
    }

    private void crashLog() {
        // draw crash logs in a file every time the application crashes
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                File crashLogFile = new File(SplashActivity.DMS_PATH + "PDM_CrashLog");
                if (!crashLogFile.exists()) {
                    crashLogFile.mkdir();
                }
                String filename = crashLogFile + "/" + sdf.format(cal.getTime()) + ".txt";

                PrintStream writer;
                try {
                    writer = new PrintStream(filename, "UTF-8");
                    writer.println(e.getClass() + ": " + e.getMessage());
                    for (int i = 0; i < e.getStackTrace().length; i++) {
                        writer.println(e.getStackTrace()[i].toString());
                    }
                    System.exit(1);
                } catch (FileNotFoundException | UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void startService() {
        final HandlerThread htd = new HandlerThread("Sync");
        htd.start();
        final Handler h = new Handler(htd.getLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                if (Params.SOURCE_PHONE_NO == null) {
                    SharedPreferences sp = getSharedPreferences("Surakshit", MODE_PRIVATE);
                    Params.SOURCE_PHONE_NO = sp.getString("phone_no", null);
                    h.postDelayed(this, 1000);
                } else {
                    final Intent syncServiceIntent = new Intent(getApplicationContext(), SyncService.class);
                    bindService(syncServiceIntent, syncServiceConnection, Context.BIND_AUTO_CREATE);
                    startService(syncServiceIntent);
                    htd.quit();
                }
            }
        });
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //default p2pConnect is off
        boolean startConnect = preferences.getBoolean("P2PConnect", false);
        boolean isXOB = preferences.getBoolean("xob_switch", false);
        if (!isXOB) {
            if (startConnect) {
                final Intent p2pServiceIntent = new Intent(getApplicationContext(), P2PConnectService.class);
                bindService(p2pServiceIntent, myServiceConnection, Context.BIND_AUTO_CREATE);
                startService(p2pServiceIntent);

            }
        } else {
            if (toggleXOB())
                Log.d("XOB","xob started");
        }
        if (!LocationState.with(MainActivity.this).locationServicesEnabled()) {
            enableGPS();
        }
        MLocation.subscribe(MainActivity.this);
    }

    private boolean toggleXOB() {
        WifiConfiguration wificonfiguration = null;
        try {
            //Change Name of the Created Hotspot
            try {
                Method getConfigMethod = wifimanager.getClass().getMethod("getWifiApConfiguration");

                WifiConfiguration wifiConfig = (WifiConfiguration) getConfigMethod.invoke(wifimanager);
//                wifiConfig.getClass().getField("apChannel").setInt(wifiConfig, 6);


                wifiConfig.allowedAuthAlgorithms.clear();
                wifiConfig.allowedGroupCiphers.clear();
                wifiConfig.allowedKeyManagement.clear();
                wifiConfig.allowedPairwiseCiphers.clear();
                wifiConfig.allowedProtocols.clear();
                wifiConfig.SSID = "DisarmHotspotDB";
                wifiConfig.preSharedKey = "password123";

                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

                Method setWifiApMethod = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                boolean apstatus = (Boolean) setWifiApMethod.invoke(wifimanager, wifiConfig, true);
                //Log.v("GetAPCOnfig:" + getConfigMethod.toString() + ",setWifiApMethod : " + setWifiApMethod.toString());
                Log.v("WifiConfig: ", wifiConfig.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, wificonfiguration, !ApManager.isApOn(this));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void unbindSyncService() {
        final Intent syncServiceIntent = new Intent(getApplicationContext(), SyncService.class);
        if (syncServiceBound) {
            unbindService(syncServiceConnection);
        }
        syncServiceBound = false;
        stopService(syncServiceIntent);
    }

    private void unbindAllService() {
        unbindSyncService();

        final Intent myServiceIntent = new Intent(getApplicationContext(), P2PConnectService.class);
        if (myServiceBound) {
            unbindService(myServiceConnection);
        }
        myServiceBound = false;
        stopService(myServiceIntent);

        if (gpsService) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            lm.removeUpdates(locationListener);
            gpsService = false;
        }
    }

    public static ServiceConnection syncServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            SyncService.SyncServiceBinder binder = (SyncService.SyncServiceBinder) service;
            syncService = binder.getService();
            syncServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            syncServiceBound = false;
        }
    };

    public static ServiceConnection myServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            P2PConnectService.P2PConnectServiceBinder binder = (P2PConnectService.P2PConnectServiceBinder) service;
            myService = binder.getService();
            myServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            myServiceBound = false;
        }
    };

    public void enableGPS() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage(R.string.gps_msg)
                .setCancelable(false)
                .setTitle("Turn on Location")
                .setPositiveButton(R.string.enable_gps,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(callGPSSettingIntent, 5);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int CONTACT_REQUEST = 100;
        if (requestCode == 5 && resultCode == 0) {
            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (provider != null) {
                switch (provider.length()) {
                    case 0:
                        //GPS still not enabled..
                        Toast.makeText(MainActivity.this, "Please enable GPS!!!", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        MLocation.subscribe(MainActivity.this);
                        Toast.makeText(this, R.string.enabled_gps, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        } else if (requestCode == CONTACT_REQUEST && resultCode == Activity.RESULT_OK &&
                data != null && data.hasExtra(ContactPickerActivity.RESULT_CONTACT_DATA)) {
            List<Contact> contacts = (List<Contact>) data.getSerializableExtra(ContactPickerActivity.RESULT_CONTACT_DATA);
            for (Contact contact : contacts) {
                // process the contacts...
                Intent intent = new Intent(this, ChatActivity.class);
                String s = contact.getPhone(0);
                String num = "";
                int count = 0;
                for (int i = s.length() - 1; i >= 0; i--) {
                    if (s.charAt(i) >= '0' && s.charAt(i) <= '9') {
                        num = num + s.charAt(i);
                        count++;
                    }
                    if (count == 10)
                        break;
                }
                StringBuilder sb = new StringBuilder(num);
                sb = sb.reverse();
                intent.putExtra("number", sb.toString());
                startActivity(intent);
            }
        } else if (requestCode == 5) {
            //the user did not enable his GPS
            enableGPS();
        }
    }


}
