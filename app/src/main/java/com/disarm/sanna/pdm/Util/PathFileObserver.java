package com.disarm.sanna.pdm.Util;

import android.os.FileObserver;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

/**
 * Created by arka on 18/9/16.
 * Detect file change using FileObserver on Android
 */
public class PathFileObserver extends FileObserver {
    static final String TAG="FILE_OBSERVER";
    /**
     * should be end with File.separator
     */
    String rootPath;
    static final int mask = (FileObserver.CREATE |
            FileObserver.DELETE |
            FileObserver.DELETE_SELF |
            FileObserver.MODIFY |
            FileObserver.MOVED_FROM |
            FileObserver.MOVED_TO |
            FileObserver.MOVE_SELF);

    public PathFileObserver(String path) {
        super(path, mask);
        if (! path.endsWith(File.separator)){
            path += File.separator;
        }
        rootPath = path;
    }

    @Override
    public void onEvent(int event, String path) {
        switch(event){
            case FileObserver.CREATE:
                Log.d(TAG, "CREATE:" + rootPath + path);
                break;
            case FileObserver.DELETE:
                Log.d(TAG, "DELETE:" + rootPath + path);
                break;
            case FileObserver.DELETE_SELF:
                Log.d(TAG, "DELETE_SELF:" + rootPath + path);
                break;
            case FileObserver.MODIFY:
                Log.d(TAG, "MODIFY:" + rootPath + path);
                break;
            case FileObserver.MOVED_FROM:
                Log.d(TAG, "MOVED_FROM:" + rootPath + path);
                break;
            case FileObserver.MOVED_TO:
                Log.d(TAG, "MOVED_TO:" + path);
                break;
            case FileObserver.MOVE_SELF:
                Log.d(TAG, "MOVE_SELF:" + path);
                break;
            default:
                // just ignore
                break;
        }
    }

    public void close() {
        super.finalize();
    }
}
