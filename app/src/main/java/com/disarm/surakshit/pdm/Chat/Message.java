package com.disarm.surakshit.pdm.Chat;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

/**
 * Created by naman on 10/2/18.
 */

public class Message implements IMessage,MessageContentType.Image, MessageContentType {
    Author author;
    String id,text;
    Date createdAt;
    boolean map,audio;

    public Message(String id,String text,Author author,boolean map,boolean audio){
        this.id = id;
        this.text = text;
        this.author = author;
        this.createdAt = new Date();
        this.map = map;
        this.audio = audio;
    }
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Author getUser() {
        return author;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public String getImageUrl() {
        return null;
    }

    public boolean getMap() { return map;}

    public boolean getAudio() { return audio;}

}
