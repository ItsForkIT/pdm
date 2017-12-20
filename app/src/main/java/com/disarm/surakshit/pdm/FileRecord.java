package com.disarm.surakshit.pdm;

/**
 * Created by naman on 10/8/17.
 */

public class FileRecord {
    String no;
    String time,source,type,path;

    public FileRecord(String no, String time, String source, String type,String path) {
        this.no = no;
        this.time = time;
        this.source = source;
        this.type = type;
        this.path = path;
    }

    public String getNo() {
        return no;
    }

    public String getPath() {
        return path;
    }




    public String getTime() {
        return time;
    }



    public String getSource() {
        return source;
    }



    public String getType() {
        return type;
    }




}
