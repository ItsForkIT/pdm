package com.disarm.surakshit.pdm.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.disarm.surakshit.pdm.Chat.Author;
import com.disarm.surakshit.pdm.Chat.ChatActivity;
import com.disarm.surakshit.pdm.Chat.DefaultDialog;

import com.disarm.surakshit.pdm.Chat.Message;
import com.disarm.surakshit.pdm.Chat.Utils.ChatUtils;
import com.disarm.surakshit.pdm.DB.DBEntities.App;
import com.disarm.surakshit.pdm.DB.DBEntities.Receiver;
import com.disarm.surakshit.pdm.DB.DBEntities.Receiver_;
import com.disarm.surakshit.pdm.DB.DBEntities.Sender;
import com.disarm.surakshit.pdm.R;
import com.disarm.surakshit.pdm.Util.ContactUtil;
import com.disarm.surakshit.pdm.Util.Params;
import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.contact.ContactDescription;
import com.onegravity.contactpicker.contact.ContactSortOrder;
import com.onegravity.contactpicker.core.ContactPickerActivity;
import com.onegravity.contactpicker.picture.ContactPictureType;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.BoxStoreBuilder;

/**
 * Created by naman on 25/2/18.
 *
 * It holds the Chat Related operations
 */

public class ChatFragment extends Fragment {
    private final int CONTACT_REQUEST = 100;
    DialogsList dialogsList;
    DialogsListAdapter<DefaultDialog> dialogsListAdapter;
    ArrayList<String> dialogID;
    HashMap<String,String> lastMsg;
    HashMap<String,Integer> unreadMap;
    HandlerThread ht;
    Handler h;
    FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat,container,false);
        dialogsList = (DialogsList) view.findViewById(R.id.dialoglist);
        dialogsListAdapter = new DialogsListAdapter<DefaultDialog>(new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                imageView.setImageDrawable(generateTextDrawable(url));
            }
        });

        fab = (FloatingActionButton) view.findViewById(R.id.btn_new_message);
        dialogsList.setAdapter(dialogsListAdapter);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startContactActivity();
            }
        });
        dialogsListAdapter.setOnDialogClickListener(new DialogsListAdapter.OnDialogClickListener<DefaultDialog>() {
            @Override
            public void onDialogClick(DefaultDialog dialog) {
                Intent i = new Intent(getActivity(), ChatActivity.class);
                i.putExtra("number",dialog.getUsers().get(0).getId());
                startActivity(i);
            }
        });
        dialogID = new ArrayList<>();
        lastMsg = new HashMap<>();
        unreadMap = new HashMap<>();
        ht = new HandlerThread("Dialog");
        ht.start();
        h = new Handler(ht.getLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                try {
                    addDialogList();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                h.postDelayed(this,1500);
            }
        });
        return view;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONTACT_REQUEST && resultCode == Activity.RESULT_OK &&
                data != null && data.hasExtra(ContactPickerActivity.RESULT_CONTACT_DATA)) {
            List<Contact> contacts = (List<Contact>) data.getSerializableExtra(ContactPickerActivity.RESULT_CONTACT_DATA);
            for (Contact contact : contacts) {
                // process the contacts...
                Intent intent = new Intent(getActivity(),ChatActivity.class);
                String s = contact.getPhone(0);
                if(s.contains("+")){
                    s = s.substring(3,s.length());
                }
                intent.putExtra("number",s);
                startActivity(intent);
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

    //Intent to start choose a contact from Contacts for new chat
    private void startContactActivity(){
        Intent intent = new Intent(getActivity(), ContactPickerActivity.class)
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_BADGE_TYPE, ContactPictureType.ROUND.name())
                .putExtra(ContactPickerActivity.EXTRA_SHOW_CHECK_ALL, false)
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION, ContactDescription.ADDRESS.name())
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_SORT_ORDER, ContactSortOrder.AUTOMATIC.name())
                .putExtra(ContactPickerActivity.EXTRA_SELECT_CONTACTS_LIMIT,1)
                .putExtra(ContactPickerActivity.EXTRA_ONLY_CONTACTS_WITH_PHONE,true);
        getActivity().startActivityForResult(intent, CONTACT_REQUEST);
    }

    private void addDialog(final Message msg ,final Author author ,final int unread){
        if(!dialogID.contains(author.getId())){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DefaultDialog dialog = new DefaultDialog(author.getId(), msg.getUser().getName(), msg, author, unread);
                    dialog.setLastMessage(msg);
                    dialogsListAdapter.addItem(dialog);
                    dialogsListAdapter.notifyDataSetChanged();
                    dialogID.add(author.getId());
                    lastMsg.put(author.getId(),msg.getText());
                    unreadMap.put(author.getId(),unread);
                }
            });
        }
        else if(dialogID.contains(author.getId()) && unread != unreadMap.get(author.getId())){
            getActivity().runOnUiThread(new Runnable() {
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
                getActivity().runOnUiThread(new Runnable() {
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
        if(getActivity()==null)
            return;


        final Box<Sender> senderBox = ((App)getActivity().getApplication()).getBoxStore().boxFor(Sender.class);
        final Box<Receiver> receiverBox = ((App)getActivity().getApplication()).getBoxStore().boxFor(Receiver.class);

        final List<Sender> senders = senderBox.getAll();
        final List<Receiver> receivers = receiverBox.getAll();

        HashMap<String,Receiver> receiverHashMap = new HashMap<>();

        for(int i=0;i<receivers.size();i++){
            receiverHashMap.put(receivers.get(i).getNumber(),receivers.get(i));
        }
        HashSet<String> receiverDone = new HashSet<>();
        for(int i=0;i<senders.size();i++){
            Sender s = senders.get(i);
            if(receiverHashMap.containsKey(s.getNumber())){
                Receiver r = receiverHashMap.get(s.getNumber());
                Author other = new Author(s.getNumber(),ContactUtil.getContactName(getActivity().getApplicationContext(),s.getNumber()));
                DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
                String senderDate = s.getLastMessage().split("-")[0];
                String receiverDate = r.getLastMessage().split("-")[0];
                Date sDate = df.parse(senderDate);
                Date rDate = df.parse(receiverDate);
                Message msg =  null;
                if(sDate.before(rDate)){
                    msg = ChatUtils.getMessageObject(r.getLastMessage(),other);
                }
                else{
                    Author me = new Author(Params.SOURCE_PHONE_NO,ContactUtil.getContactName(getActivity().getApplicationContext(),r.getNumber()));
                    msg = ChatUtils.getMessageObject(s.getLastMessage(),me);
                }
                int unread = r.getUnread();
                addDialog(msg,other,unread);
                receiverDone.add(r.getNumber());
            }
            else{
                Author author = new Author(s.getNumber(),ContactUtil.getContactName(getActivity().getApplicationContext(),s.getNumber()));
                Author me =  new Author(Params.SOURCE_PHONE_NO,ContactUtil.getContactName(getActivity().getApplicationContext(),s.getNumber()));
                Message msg = ChatUtils.getMessageObject(s.getLastMessage(),me);
                addDialog(msg,author,0);
            }
        }

        for(int i=0;i<receivers.size();i++){
            Receiver r = receivers.get(i);
            if(receiverDone.contains(r.getNumber())) {

            }
            else{
                Author author = new Author(r.getNumber(),ContactUtil.getContactName(getActivity().getApplicationContext(),r.getNumber()));
                Message msg = ChatUtils.getMessageObject(r.getLastMessage(),author);
                addDialog(msg,author,r.getUnread());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
