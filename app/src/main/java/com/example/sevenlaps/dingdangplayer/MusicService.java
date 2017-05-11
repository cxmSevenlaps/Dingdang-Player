package com.example.sevenlaps.dingdangplayer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.sevenlaps.controller.PlayController;
import com.example.sevenlaps.orm.DatabaseModel;

public class MusicService extends Service {
    private static final String MUSIC_SERVICE_LOG="MusicService";
    private PlayController mPlayController;

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {

        Log.d(MUSIC_SERVICE_LOG, "MusicService onCreate()");
        mPlayController = PlayController.getInstance();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(MUSIC_SERVICE_LOG, "MusicService onStartCommand()");
        int id = intent.getIntExtra("id", -1);

        if (-1!=id){
            Log.d(MUSIC_SERVICE_LOG, " id="+id);
            MusicItem item = DatabaseModel.getDatabaseModelInstance(this).getMusicItemById(id);
            if (null!=item) {
                mPlayController.initMediaPlayer(item.getPath());
                mPlayController.play();
            }
        }


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        mPlayController.destroy();
    }
}
