package com.disarm.surakshit.pdm.Chat;

import com.stfalcon.chatkit.commons.models.IDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by naman on 25/2/18.
 *
 * It defines a single chat dialog
 */

public class DefaultDialog implements IDialog<Message> {

    String id,name;
    Message lastMessage;
    ArrayList<Author> author;
    int unread;

    public DefaultDialog(String id,String name, Message lastMessage, Author author, int unread){
        this.id = id;
        this.name = name;
        this.unread = unread;
        this.author = new ArrayList<Author>();
        this.author.add(author);
    }
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDialogPhoto() {
        return name.charAt(0)+"";
    }

    @Override
    public String getDialogName() {
        return name;
    }

    @Override
    public List<Author> getUsers() {
        return author;
    }

    @Override
    public Message getLastMessage() {
        return lastMessage;
    }

    @Override
    public void setLastMessage(Message message) {
        lastMessage = message;
    }

    @Override
    public int getUnreadCount() {
        return unread;
    }
}
