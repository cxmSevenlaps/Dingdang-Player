package com.example.sevenlaps.dingdangplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.sevenlaps.controller.PlayController;
import com.example.sevenlaps.orm.DatabaseModel;

import java.io.IOException;

public class MusicService extends Service {

    private static final String LOG_TAG="MusicService";
    private final IBinder musicBinder = new MusicBinder();
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private PlayController mPlayController = PlayController.getInstance();
    private int mMusicId;

    public MusicService() {

        initMediaPlayerFile();
    }

    public void playMusic(){
        if (!mMediaPlayer.isPlaying()){
            mMediaPlayer.start();
        }
    }

    public void pauseMusic(){
        if (mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
        }
    }

    public void resetMusic(){
        mMediaPlayer.reset();
        initMediaPlayerFile();
    }

    public void stopMusic(){
        if (mMediaPlayer!=null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }



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

    private void initMediaPlayerFile(){
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(DatabaseModel.getDatabaseModelInstance(this)
                    .getMusicItemById(mPlayController.getIsPlayingId()).getPath());
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
