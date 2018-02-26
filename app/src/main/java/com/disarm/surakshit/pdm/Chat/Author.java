package com.disarm.surakshit.pdm.Chat;

import com.stfalcon.chatkit.commons.models.IUser;

/**
 * Created by naman on 10/2/18.
 */



public class Author implements IUser {
    String id,name;
    public Author(String id, String name){
        this.id = id;
        this.name = name;
    }
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return null;
    }

}

