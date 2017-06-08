package com.example.sevenlaps.utils;

import android.app.Application;
import android.app.Service;

import com.example.sevenlaps.dingdangplayer.MusicService;

/**
 * Created by chenxianmin on 2017/6/8.
 */

public class DingdangApplication extends Application {

    private MusicService mService;
    private static DingdangApplication mApp = new DingdangApplication();

    public DingdangApplication() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static DingdangApplication getDingdangApplication(){
        return mApp;
    }
    public MusicService getmService() {
        return mService;
    }

    public void setmService(MusicService mService) {
        this.mService = mService;
    }
}
