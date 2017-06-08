package com.example.sevenlaps.utils;

import android.app.Application;

import com.example.sevenlaps.dingdangplayer.MusicService;

/**
 * Created by chenxianmin on 2017/6/8.
 */

public class DingdangApplication extends Application {

    private MusicService mService;
    private boolean mIsBound = false;//界面是否和服务绑定标识。标志着是否可以取到服务单例。还没绑定服务,耳机插入播放歌曲会出现空指针
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

    public boolean ismIsBound() {
        return mIsBound;
    }

    public void setmIsBound(boolean mIsBound) {
        this.mIsBound = mIsBound;
    }
}
