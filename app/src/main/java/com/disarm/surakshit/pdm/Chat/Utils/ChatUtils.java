package com.disarm.surakshit.pdm.Chat.Utils;

import android.os.Environment;
import android.widget.Toast;

import com.disarm.surakshit.pdm.Chat.Author;
import com.disarm.surakshit.pdm.Chat.ChatActivity;
import com.disarm.surakshit.pdm.Chat.Message;
import com.disarm.surakshit.pdm.Util.Params;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by naman on 11/3/18.
 */

public class ChatUtils {

    
    public static String getExtendedDataFormatName(String text,String type,String mapID){
        return getTimeStamp()+"-"+type+"-"+mapID+"-"+text;
    }

    public static String getTimeStamp(){
        return new SimpleDateFormat("yyyyMMddHHmmss",Locale.ENGLISH).format(new Date());
    }

    //Get a Message object from a extended data format string
    public static Message getMessageObject(String extendedData, Author author){
        Message message = null;
        Pattern p = Pattern.compile("-");
        String[] s = p.split(extendedData,4);
        String date = s[0];
        String type = s[1];
        String mapObjectId = s[2];
        String text = s[3];
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
        try {
            Date d = df.parse(date);
            message = new Message(date, author, type, d);
            if(type.equals("text")){
                message.setText(text);
            }
            else if(type.equals("image")){
                String url = "DMS/Working/SurakshitImages/"+text;
                message.setImageurl(url);
            }
            else if(type.equals("map")){
                String url = Environment.getExternalStoragePublicDirectory("DMS/Working/SurakshitMap/"+text).getAbsolutePath();
                message.setImageurl(url);
                message.setMapObjectID(mapObjectId);
            }
            else if(type.equals("audio")){
                String url = Environment.getExternalStoragePublicDirectory("DMS/Working/SurakshitAudio/"+text).getAbsolutePath();
                message.setUrl(url);
            }
            else if(type.equals("video")){
                String url = Environment.getExternalStoragePublicDirectory("DMS/Working/SurakshitVideos/"+text).getAbsolutePath();
                message.setUrl(url);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return message;
    }


}
