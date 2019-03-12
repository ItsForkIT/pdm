package com.disarm.surakshit.pdm.DB.DBEntities;

import android.app.Application;
import android.content.Context;

import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;
import io.objectbox.android.BuildConfig;

/**
 * Created by naman on 13/3/18.
 */

public class App extends Application {

    public static final String TAG = "ObjectBoxExample";
    public static final boolean EXTERNAL_DIR = false;
    private static App instance;
    private BoxStore boxStore;

    @Override
    public void onCreate() {
        super.onCreate();
        boxStore = MyObjectBox.builder().androidContext(App.this).build();
        if (BuildConfig.DEBUG) {
            new AndroidObjectBrowser(boxStore).start(this);
        }
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }

    public static App getApplication() {
        return instance;
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }
}
