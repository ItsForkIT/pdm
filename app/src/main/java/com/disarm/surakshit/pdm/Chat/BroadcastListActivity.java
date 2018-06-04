package com.disarm.surakshit.pdm.Chat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.disarm.surakshit.pdm.Chat.Utils.ChatUtils;
import com.disarm.surakshit.pdm.DB.DBEntities.App;
import com.disarm.surakshit.pdm.DB.DBEntities.Receiver;
import com.disarm.surakshit.pdm.DB.DBEntities.Receiver_;
import com.disarm.surakshit.pdm.DB.DBEntities.Sender;
import com.disarm.surakshit.pdm.DB.DBEntities.Sender_;
import com.disarm.surakshit.pdm.R;
import com.disarm.surakshit.pdm.Util.ContactUtil;
import com.disarm.surakshit.pdm.Util.Params;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import io.objectbox.Box;

public class BroadcastListActivity extends AppCompatActivity {
    DialogsList dialogsList;
    DialogsListAdapter<DefaultDialog> dialogsListAdapter;
    HashMap<String, String> lastMsg;
    HashMap<String, Integer> unreadMap;
    ArrayList<String> dialogID;
    String from;
    String source_no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_list);
        dialogID = new ArrayList<>();
        lastMsg = new HashMap<>();
        unreadMap = new HashMap<>();
        dialogsList = findViewById(R.id.chat_dialog_broadcast);
        dialogsListAdapter = new DialogsListAdapter<DefaultDialog>(new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                imageView.setImageDrawable(generateTextDrawable(url));
            }
        });
        dialogsList.setAdapter(dialogsListAdapter);
        from = getIntent().getStringExtra("from");
        if(Params.WHO.equalsIgnoreCase("volunteer")){
            source_no = "v"+Params.SOURCE_PHONE_NO;
        }
        else{
            source_no = Params.SOURCE_PHONE_NO;
        }

        try {
            addDialogList();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dialogsListAdapter.setOnDialogClickListener(new DialogsListAdapter.OnDialogClickListener<DefaultDialog>() {
            @Override
            public void onDialogClick(DefaultDialog dialog) {
                Intent i = new Intent(BroadcastListActivity.this,ChatActivity.class);
                i.putExtra("from",from);
                i.putExtra("number",dialog.getId());
                i.putExtra("reply",false);
                startActivity(i);
            }
        });
    }

    private void addDialog(final Message msg ,final Author author ,final int unread){
        Log.d("BROADCAST","ADD DIALOG");
        if(!dialogID.contains(author.getId())){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("BROADCAST","No");
                    DefaultDialog dialog = new DefaultDialog(author.getId(), msg.getUser().getName(), msg, author, unread);
                    dialog.setLastMessage(msg);
                    dialogsListAdapter.addItem(dialog);
                    Log.d("BROADCAST","Add item");
                    dialogsListAdapter.notifyDataSetChanged();
                    Log.d("BROADCAST","Dataset changed");
                    dialogID.add(author.getId());
                    lastMsg.put(author.getId(),msg.getText());
                    unreadMap.put(author.getId(),unread);
                }
            });
        }
        else if(dialogID.contains(author.getId()) && unread != unreadMap.get(author.getId())){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialogsListAdapter.deleteById(author.getId());
                    dialogsListAdapter.notifyDataSetChanged();
                    DefaultDialog dialog = new DefaultDialog(author.getId(), msg.getUser().getName(), msg, author, unread);
                    dialog.setLastMessage(msg);
                    dialogsListAdapter.addItem(dialog);
                    dialogsListAdapter.notifyDataSetChanged();
                    dialogsListAdapter.sortByLastMessageDate();
                    unreadMap.put(author.getId(),unread);
                }
            });
        }
        else if(dialogID.contains(author.getId())){
            String oldMsg = lastMsg.get(author.getId());
            if(!oldMsg.equals(msg.getText())){
                lastMsg.put(author.getId(),msg.getText());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogsListAdapter.deleteById(author.getId());
                        dialogsListAdapter.notifyDataSetChanged();
                        DefaultDialog dialog = new DefaultDialog(author.getId(), msg.getUser().getName(), msg, author, unread);
                        dialog.setLastMessage(msg);
                        dialogsListAdapter.addItem(dialog);
                        dialogsListAdapter.notifyDataSetChanged();
                        dialogsListAdapter.sortByLastMessageDate();
                    }
                });
            }
        }



    }

    //Add dialogs available in the db
    private void addDialogList() throws ParseException {

        final Box<Sender> senderBox = ((App)getApplication()).getBoxStore().boxFor(Sender.class);
        final Box<Receiver> receiverBox = ((App)getApplication()).getBoxStore().boxFor(Receiver.class);

        final List<Sender> senders;
        final List<Receiver> receivers;

        if(from.equalsIgnoreCase("user")){
            senders = senderBox.query().equal(Sender_.forUser,true).build().find();
            receivers = receiverBox.query().equal(Receiver_.forUser,true).build().find();
        }
        else{
            senders = senderBox.query().equal(Sender_.forVolunteer,true).build().find();
            receivers = receiverBox.query().equal(Receiver_.forVolunteer,true).build().find();
        }
        HashMap<String,Receiver> receiverHashMap = new HashMap<>();

        for(int i=0;i<receivers.size();i++){
            receiverHashMap.put(receivers.get(i).getNumber(),receivers.get(i));
        }
        HashSet<String> receiverDone = new HashSet<>();
        for(int i=0;i<senders.size();i++){
            Sender s = senders.get(i);
            Log.d("BROADCAST","Sender Size");
            if(receiverHashMap.containsKey(s.getNumber())){
                Receiver r = receiverHashMap.get(s.getNumber());
                Author other = new Author(s.getNumber(), ContactUtil.getContactName(getApplicationContext(),s.getNumber()));
                DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
                String senderDate = s.getLastMessage().split("-")[0];
                String receiverDate = r.getLastMessage().split("-")[0];
                Date sDate = df.parse(senderDate);
                Date rDate = df.parse(receiverDate);
                Message msg;
                if(sDate.before(rDate)){
                    msg = ChatUtils.getMessageObject(r.getLastMessage(),other);
                }
                else{
                    Author me = new Author(source_no,ContactUtil.getContactName(getApplicationContext(),r.getNumber()));
                    msg = ChatUtils.getMessageObject(s.getLastMessage(),me);
                }
                int unread = r.getUnread();
                addDialog(msg,other,unread);
                receiverDone.add(r.getNumber());
            }
            else{
                try {
                    Author author = new Author(s.getNumber(), ContactUtil.getContactName(getApplicationContext(), s.getNumber()));
                    Author me = new Author(source_no, ContactUtil.getContactName(getApplicationContext(), s.getNumber()));
                    Message msg = ChatUtils.getMessageObject(s.getLastMessage(), me);
                    addDialog(msg, author, 0);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        for(int i=0;i<receivers.size();i++){
            Log.d("BROADCAST","Receiver Size");
            Receiver r = receivers.get(i);
            if(receiverDone.contains(r.getNumber())) {

            }
            else{
                Author author = new Author(r.getNumber(),ContactUtil.getContactName(getApplicationContext(),r.getNumber()));
                Message msg = ChatUtils.getMessageObject(r.getLastMessage(),author);
                addDialog(msg,author,r.getUnread());
            }
        }
    }


    //Creates a simple Drawable image of initial letter of the name
    private TextDrawable generateTextDrawable(String url){
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color1 = generator.getRandomColor();
        TextDrawable.IBuilder builder = TextDrawable.builder()
                .beginConfig()
                .withBorder(4)
                .endConfig()
                .round();
        return builder.build(url, color1);
    }
}
