package com.disarm.surakshit.pdm.Chat;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.disarm.surakshit.pdm.Chat.Holders.IncomingAudioHolders;
import com.disarm.surakshit.pdm.Chat.Holders.IncomingVideoHolders;
import com.disarm.surakshit.pdm.Chat.Holders.OutgoingAudioHolders;
import com.disarm.surakshit.pdm.Chat.Holders.OutgoingVideoHolders;
import com.disarm.surakshit.pdm.Chat.Utils.ChatUtils;
import com.disarm.surakshit.pdm.DB.DBEntities.App;
import com.disarm.surakshit.pdm.DB.DBEntities.Receiver;
import com.disarm.surakshit.pdm.DB.DBEntities.Receiver_;
import com.disarm.surakshit.pdm.DB.DBEntities.Sender;
import com.disarm.surakshit.pdm.DB.DBEntities.Sender_;
import com.disarm.surakshit.pdm.Encryption.KeyBasedFileProcessor;
import com.disarm.surakshit.pdm.R;
import com.disarm.surakshit.pdm.Util.ContactUtil;
import com.disarm.surakshit.pdm.Util.DiffUtils;
import com.disarm.surakshit.pdm.Util.Params;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Duration;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.snatik.storage.Storage;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.osmdroid.bonuspack.kml.KmlDocument;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import io.objectbox.Box;

public class ChatActivity extends AppCompatActivity implements MessageHolders.ContentChecker<Message> {
    MessagesList messagesList;
    MessageInput messageInput;
    ImageLoader load;
    String number;
    Author me,other;
    MessagesListAdapter<Message> messagesListAdapter;
    ArrayList<Message> allMessages;
    int total_msg_receiver=0;
    private final byte CONTENT_AUDIO=1,CONTENT_VIDEO=2;
    HandlerThread ht;
    Handler h;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messagesList = (MessagesList) findViewById(R.id.messagesList);
        messageInput = (MessageInput) findViewById(R.id.input);
        ActionBar ab = getSupportActionBar();
        Drawable d = getResources().getDrawable(R.color.fbutton_color_turquoise);
        if (ab != null) {
            ab.setBackgroundDrawable(d);
        }

        ht = new HandlerThread("newMsg");
        ht.start();
        h = new Handler(ht.getLooper());

        number = getIntent().getStringExtra("number");
        String receiversName = ContactUtil.getContactName(getApplicationContext(),number);
        me = new Author(Params.SOURCE_PHONE_NO,"Me");
        other = new Author(number,receiversName);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(receiversName);
        }

        setChatConfig();

        setMessagesListAdapterListener();

        setMessageInputAttachmentListener();

        populateChat();

        setMessageInputSendListener();

        h.post(new Runnable() {
            @Override
            public void run() {
                final Box<Receiver> receiverBox = ((App)getApplication()).getBoxStore().boxFor(Receiver.class);
                List<Receiver> receivers = receiverBox.query().equal(Receiver_.number,number).build().find();
                if(receivers.size()!=0){
                    Receiver receiver = receivers.get(0);
                    if(total_msg_receiver < receiver.getTotalMsg()){
                        populateChat();
                    }
                }
                h.postDelayed(this,1000);
            }
        });

    }

    @Override
    public boolean hasContentFor(Message message, byte type) {
        if(type == CONTENT_AUDIO){
            return message.isAudio();
        }
        if(type == CONTENT_VIDEO){
            return message.isVideo();
        }
        return false;
    }

    private void populateChat(){
        messagesListAdapter.clear();
        messagesListAdapter.notifyDataSetChanged();
        final Box<Sender> senderBox = ((App)getApplication()).getBoxStore().boxFor(Sender.class);
        final Box<Receiver> receiverBox = ((App)getApplication()).getBoxStore().boxFor(Receiver.class);
        List<Sender> senders = senderBox.query().equal(Sender_.number,number).build().find();
        List<Receiver> receivers = receiverBox.query().equal(Receiver_.number,number).build().find();
        KmlDocument senderKml = new KmlDocument();
        KmlDocument receiversKml = new KmlDocument();
        if(senders.size() != 0 ) {
            String sendersKml = senders.get(0).getKml();
            InputStream sendersStream = new ByteArrayInputStream(sendersKml.getBytes(StandardCharsets.UTF_8));
            senderKml.parseKMLStream(sendersStream, null);
        }
        if(receivers.size() != 0){
            String receiverKml = receivers.get(0).getKml();
            InputStream receiversStream = new ByteArrayInputStream(receiverKml.getBytes(StandardCharsets.UTF_8));
            receiversKml.parseKMLStream(receiversStream, null);
        }
        extractMessageFromKML(senderKml,receiversKml);
        senderBox.closeThreadResources();
        receiverBox.closeThreadResources();
    }

    private void extractMessageFromKML(final KmlDocument sender,final KmlDocument receiver){

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                allMessages = new ArrayList<>();
                String nextKey = "source";
                String msg;
                while (sender.mKmlRoot.mExtendedData!=null && sender.mKmlRoot.mExtendedData.containsKey(nextKey)){
                    msg = sender.mKmlRoot.getExtendedData(nextKey);
                    nextKey = getTimeStampFromMsg(msg);
                    allMessages.add(ChatUtils.getMessageObject(msg, me));
                }
                nextKey = "source";

                while (receiver.mKmlRoot.mExtendedData!=null && receiver.mKmlRoot.mExtendedData.containsKey(nextKey)){
                    msg = receiver.mKmlRoot.getExtendedData(nextKey);
                    nextKey = getTimeStampFromMsg(msg);
                    allMessages.add(ChatUtils.getMessageObject(msg, other));
                    total_msg_receiver++;
                }
                sortAllMessage();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messagesListAdapter.addToEnd(allMessages,false);
                    }
                });

            }
        });
        t.start();

    }

    private void sortAllMessage(){
        Collections.sort(allMessages, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                if (o1.getCreatedAt().after(o2.getCreatedAt())) {
                    return -1;
                } else if (o1.getCreatedAt().before(o2.getCreatedAt())) {
                    return 1;
                } else return 0;
            }
        });
    }

    private String getTimeStampFromMsg(String msg){
        Pattern p = Pattern.compile("-");
        String[] s = p.split(msg,4);
        return s[0];
    }

    private void setChatConfig(){
        load = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                File im = Environment.getExternalStoragePublicDirectory(url);
                if(im.exists()) {
                    Picasso.with(ChatActivity.this).load(im).resize(800,1000).centerCrop().into(imageView);
                }
            }
        };
        MessageHolders holders = new MessageHolders();
        holders.registerContentType(CONTENT_AUDIO,
                IncomingAudioHolders.class,R.layout.chat_incoming_audio,
                OutgoingAudioHolders.class,R.layout.chat_outgoing_audio,
                this);
        holders.registerContentType(CONTENT_VIDEO,
                IncomingVideoHolders.class,R.layout.chat_incoming_video,
                OutgoingVideoHolders.class,R.layout.chat_outgoing_video,
                this);

        messagesListAdapter = new MessagesListAdapter<Message>(Params.SOURCE_PHONE_NO,holders,load);
    }

    private void setMessagesListAdapterListener(){
        messagesListAdapter.setOnMessageClickListener(new MessagesListAdapter.OnMessageClickListener<Message>() {
            @Override
            public void onMessageClick(Message message) {

                //Define what to do with msg touch events
                //Video touch and Audio touch are already defined in the holders

                if(message.isImage()){
                    Intent i = new Intent(ChatActivity.this, ImageViewActivity.class);
                    i.putExtra("url",message.getImageUrl());
                    startActivity(i);
                }
            }
        });
        messagesList.setAdapter(messagesListAdapter);
    }

    private void setMessageInputAttachmentListener(){
        messageInput.setAttachmentsListener(new MessageInput.AttachmentsListener() {
            @Override
            public void onAddAttachments() {
                View view = getLayoutInflater().inflate(R.layout.dialog_attachment,null);
                MaterialStyledDialog materialDialog = new MaterialStyledDialog.Builder(ChatActivity.this)
                        .setTitle(R.string.attachment)
                        .setCustomView(view,10,20,10,20)
                        .withDialogAnimation(true, Duration.FAST)
                        .setCancelable(true)
                        .setStyle(Style.HEADER_WITH_TITLE)
                        .withDarkerOverlay(true)
                        .build();

                Window window = materialDialog.getWindow();
                WindowManager.LayoutParams wlp = null;
                if (window != null) {
                    wlp = window.getAttributes();
                }
                assert wlp != null;
                wlp.gravity = Gravity.BOTTOM;
                wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                window.setAttributes(wlp);
                materialDialog.show();
            }
        });
    }

    private void setMessageInputSendListener(){
        messageInput.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                if(messagesListAdapter.getItemCount() == 0){
                    KmlDocument kml = new KmlDocument();
                    String extendedDataFormat = ChatUtils.getExtendedDataFormatName(input.toString(),"text","none");
                    kml.mKmlRoot.setExtendedData("source",extendedDataFormat);
                    kml.mKmlRoot.setExtendedData("total","1");
                    File file = getNewFileObject();
                    kml.saveAsKML(file);
                    File dest = Environment.getExternalStoragePublicDirectory("DMS/KML/Source/LatestKml/"+FilenameUtils.getBaseName(file.getName())+".kml");
                    try {
                        FileUtils.copyFile(file,dest);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final Box<Sender> senderBox = ((App)getApplication()).getBoxStore().boxFor(Sender.class);
                    Sender sender = new Sender();
                    sender.setNumber(number);
                    sender.setLastMessage(extendedDataFormat);
                    sender.setLastUpdated(true);
                    String kmlString="";
                    try {
                        kmlString = FileUtils.readFileToString(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sender.setKml(kmlString);
                    senderBox.put(sender);
                    senderBox.closeThreadResources();
                    populateChat();
                    encryptIt(file);
                }
                else{
                    KmlDocument kml = new KmlDocument();
                    File latestSourceDir = Environment.getExternalStoragePublicDirectory("DMS/KML/Source/LatestKml");
                    File kmlFile = null;
                    for(File file : latestSourceDir.listFiles()){
                        if(file.getName().contains(number)){
                            kml.parseKMLFile(file);
                            kmlFile =file;
                            break;
                        }
                    }
                    String nextKey = "source";
                    String msg = "";
                    while(kml.mKmlRoot.mExtendedData.containsKey(nextKey)){
                        msg = kml.mKmlRoot.getExtendedData(nextKey);
                        nextKey = getTimeStampFromMsg(msg);
                    }
                    String extendedDataFormat = ChatUtils.getExtendedDataFormatName(input.toString(),"text","none");
                    kml.mKmlRoot.setExtendedData(nextKey,extendedDataFormat);
                    int total = Integer.parseInt(kml.mKmlRoot.getExtendedData("total"));
                    total++;
                    kml.mKmlRoot.setExtendedData("total",total+"");
                    kml.saveAsKML(kmlFile);
                    final Box<Sender> senderBox = ((App)getApplication()).getBoxStore().boxFor(Sender.class);
                    List<Sender> senderList = senderBox.query().contains(Sender_.number,number).build().find();
                    Sender s = senderList.get(0);
                    s.setLastUpdated(true);
                    s.setLastMessage(extendedDataFormat);
                    String kmlString="";
                    try {
                        assert kmlFile != null;
                        kmlString = FileUtils.readFileToString(kmlFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    s.setKml(kmlString);
                    senderBox.put(s);
                    senderBox.closeThreadResources();
                    populateChat();
                    generateDiff(kmlFile);
                }
                return true;
            }
        });
    }

    //Generates a 16-bit unique random string
    private String generateRandomString(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[16];
        secureRandom.nextBytes(token);
        return new BigInteger(1, token).toString(16);
    }

    private File getNewFileObject(){
        String fileName = generateRandomString() + "_" + Params.SOURCE_PHONE_NO + "_" + number + "_" + "50";
        return Environment.getExternalStoragePublicDirectory("DMS/KML/Source/SourceKml/"+fileName);
    }

    private void encryptIt(File file){
        String inputPath = file.getAbsolutePath();
        String publicKeyPath = Environment.getExternalStoragePublicDirectory("DMS/Working/pgpKey/pub_"+number+".bgp").getAbsolutePath();
        String outputFilePath = Environment.getExternalStoragePublicDirectory("DMS/Working/SurakshitKml/"
                +FilenameUtils.getBaseName(file.getName())+".bgp")
                .getAbsolutePath();
        try {
            KeyBasedFileProcessor.encrypt(inputPath,publicKeyPath,outputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateDiff(final File dest){
        final HandlerThread ht = new HandlerThread("Diff");
        ht.start();
        Handler diffHandler = new Handler(ht.getLooper());
        diffHandler.post(new Runnable() {
            @Override
            public void run() {
                File source =null;
                File sourceDir = Environment.getExternalStoragePublicDirectory("DMS/KML/Source/SourceKml");
                for(File file : sourceDir.listFiles()){
                    if(file.getName().contains(number)){
                        source =file;
                        break;
                    }
                }
                try {
                    DiffUtils.createDiff(source,dest);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ht.quit();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ht.quit();
    }
}
