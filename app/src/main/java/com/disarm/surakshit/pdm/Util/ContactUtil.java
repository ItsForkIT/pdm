package com.disarm.surakshit.pdm.Util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;
import com.github.tamir7.contacts.Query;
import com.onegravity.contactpicker.contact.ContactDescription;
import com.onegravity.contactpicker.contact.ContactSortOrder;
import com.onegravity.contactpicker.core.ContactPickerActivity;
import com.onegravity.contactpicker.picture.ContactPictureType;

import java.util.List;

/**
 * Created by naman on 25/2/18.
 */

public class ContactUtil {

    //Requires Application Context as Param
    public static String getContactName(Context appContext,@NonNull String phoneNo){
        Contacts.initialize(appContext);
        Query q = Contacts.getQuery();
        q.whereContains(com.github.tamir7.contacts.Contact.Field.PhoneNumber,phoneNo);
        List<Contact> con = q.find();
        if(con.size() == 0){
            return phoneNo;
        }
        return con.get(0).getDisplayName();
    }

}
