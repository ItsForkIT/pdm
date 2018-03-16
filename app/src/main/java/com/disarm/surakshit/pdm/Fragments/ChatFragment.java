package com.disarm.surakshit.pdm.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    ArrayList<DefaultDialog> dialogArrayList;
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

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.btn_new_message);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        startContactActivity();
            }
        });
        dialogsList.setAdapter(dialogsListAdapter);

        dialogsListAdapter.setOnDialogClickListener(new DialogsListAdapter.OnDialogClickListener<DefaultDialog>() {
            @Override
            public void onDialogClick(DefaultDialog dialog) {
                Intent i = new Intent(getActivity(), ChatActivity.class);
                i.putExtra("number",dialog.getUsers().get(0).getId());
                startActivity(i);
            }
        });


        //Dummy chat
        Author me = new Author("9000000001","Anuj");
        Message msg9 = new Message("9",me,"text");
        msg9.setText("Okay... I am here");
        addDialog(msg9,me,2);

        addDialogList();


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

                Toast.makeText(getContext(),contact.getFirstName(),Toast.LENGTH_SHORT).show();
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
        TextDrawable ic1 = builder.build(url, color1);
        return ic1;
    }

    //Intent to start choose a contact from Contacts for new chat
    private void startContactActivity(){
        Intent intent = new Intent(getActivity(), ContactPickerActivity.class)
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_BADGE_TYPE, ContactPictureType.ROUND.name())
                .putExtra(ContactPickerActivity.EXTRA_SHOW_CHECK_ALL, true)
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION, ContactDescription.ADDRESS.name())
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_SORT_ORDER, ContactSortOrder.AUTOMATIC.name())
                .putExtra(ContactPickerActivity.EXTRA_SELECT_CONTACTS_LIMIT,1);
        startActivityForResult(intent, CONTACT_REQUEST);
    }

    private void addDialog(Message msg , Author author , int unread){
        DefaultDialog dialog = new DefaultDialog(Params.dialog_id++ +"",msg.getUser().getName(),msg,author,unread);
        dialog.setLastMessage(msg);
        dialogsListAdapter.addItem(dialog);
    }


    //Add dialogs available in the db
    private void addDialogList(){
        final Box<Sender> senderBox = ((App)getActivity().getApplication()).getBoxStore().boxFor(Sender.class);
        final Box<Receiver> receiverBox = ((App)getActivity().getApplication()).getBoxStore().boxFor(Receiver.class);
        final List<Sender> senders = senderBox.getAll();
        dialogArrayList = new ArrayList<>();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<senders.size();i++){
                    Sender s = senders.get(i);
                    if((s.getLastUpdated())){
                        String msg = s.getLastMessage();
                        String number = s.getNumber();
                        Author author = new Author(number, ContactUtil.getContactName(getActivity().getApplicationContext(),number));
                        Message lastMessage = ChatUtils.getMessageObject(msg,author);
                        addDialog(lastMessage,author,0);
                    }
                    else{
                        String number = s.getNumber();
                        List<Receiver> re = receiverBox.query().equal(Receiver_.number,number).build().find();
                        Receiver r = re.get(0);
                        Author author = new Author(number, ContactUtil.getContactName(getActivity().getApplicationContext(),number));
                        String msg = r.getLastMessage();
                        Message lastMessage = ChatUtils.getMessageObject(msg,author);
                        addDialog(lastMessage,author,r.getUnread());
                    }
                }
                dialogsListAdapter.sortByLastMessageDate();
            }
        });
        t.start();
    }
}
