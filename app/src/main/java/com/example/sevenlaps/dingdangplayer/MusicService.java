package com.example.sevenlaps.dingdangplayer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.sevenlaps.controller.PlayController;

public class MusicService extends Service {

    private static final String LOG_TAG="MusicService";
    private final IBinder musicBinder = new MusicBinder();
    private PlayController mPlayController = PlayController.getInstance();
    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG,"onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG,"onStartCommand(Intent intent, int flags, int startId)");
        return super.onStartCommand(intent, flags, startId);
    }

    class MusicBinder extends Binder{
        MusicService getService(){
            Log.d(LOG_TAG, "getService()");
            return MusicService.this;
        }



    }
}
