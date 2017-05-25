package com.example.sevenlaps.dingdangplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.sevenlaps.controller.PlayController;
import com.example.sevenlaps.controller.PlayStateConstant;
import com.example.sevenlaps.orm.DatabaseModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {
    public interface OnMusicStateChangedListener {
        public void onMusicStateChanged(int playState);
    }

    private List<OnMusicStateChangedListener> musicStateChangedListeners;
    private int playState = PlayStateConstant.IS_STOP;//播放状态
    private int playingId = 1;//正在播放的歌曲id,默认第一首
    private String path;

    private static final String LOG_TAG = "MusicService";
    private final IBinder musicBinder = new MusicBinder();
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private PlayController mPlayController = PlayController.getInstance();


    public MusicService() {

        initMediaPlayerFile();
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            Log.d(LOG_TAG, "getService()");
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    public int getPlayState() {
        return playState;
    }

    public void setPlayState(int playState) {
        this.playState = playState;
    }

    public int getPlayingId() {
        return playingId;
    }

    public void setPlayingId(int playingId) {
        this.playingId = playingId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void playMusic(MusicItem item) {

//        if (!mMediaPlayer.isPlaying()){
//            mMediaPlayer.start();
//        }

        Log.d(LOG_TAG, "playMusic(MusicItem item)");
        switch (getPlayState()) {
            case PlayStateConstant.ISPLAYING:
            case PlayStateConstant.ISPAUSE:
                if (item.getmId() != playingId) {
                    mMediaPlayer.reset();
                    Log.d(LOG_TAG, "mediaplayer reset:换歌");
                    try {
                        mMediaPlayer.setDataSource(item.getPath());
                        mMediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mMediaPlayer.start();

                } else {   //在列表中点击同一首歌
                    if (playState == PlayStateConstant.ISPLAYING) {
                        Log.d(LOG_TAG, "已经在播放: " + item.getMusicTitle() + ",因此啥也不用做");
                    } else if (playState == PlayStateConstant.ISPAUSE) {//如果是暂停状态,点击后继续播放
                        Log.d(LOG_TAG, "继续播放: " + item.getMusicTitle());
                        mMediaPlayer.start();
                    }
                }
                break;
            case PlayStateConstant.IS_STOP://首次打开app
                mMediaPlayer = new MediaPlayer();
                try {
                    mMediaPlayer.setDataSource(item.getPath());
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mMediaPlayer.start();

                break;
            default:break;
        }

//        mPlayController.setPlayState(PlayStateConstant.ISPLAYING);
//        mPlayController.setIsPlayingId(item.getmId());
//        notifyStateChanged(mPlayController.getPlayState());
        playState=PlayStateConstant.ISPLAYING;
        playingId=item.getmId();
        notifyStateChanged(playState);

    }
    public void playOrPause() {
        Log.d(LOG_TAG, " playOrPause()");
        switch (playState) {
            case PlayStateConstant.ISPLAYING:
                mMediaPlayer.pause();
                setPlayState(PlayStateConstant.ISPAUSE);
                break;
            case PlayStateConstant.ISPAUSE:
                mMediaPlayer.start();
                setPlayState(PlayStateConstant.ISPLAYING);
                break;
            case PlayStateConstant.IS_STOP://首次打开app,点击播放

                try {
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setDataSource(path);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                    setPlayState(PlayStateConstant.ISPLAYING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        notifyStateChanged(playState);
    }


    public void playMusic() {
        Log.d(LOG_TAG, "playMusic()");
        if (!mMediaPlayer.isPlaying()) {
            //如果还没开始播放，就开始
            mMediaPlayer.start();
        }
    }
    public void pauseMusic() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void resetMusic() {
        mMediaPlayer.reset();
        initMediaPlayerFile();
    }

    public void stopMusic() {
        Log.d(LOG_TAG, "stopMusic()");
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate()");
        musicStateChangedListeners = new ArrayList<OnMusicStateChangedListener>();//初始化监听列表
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand(Intent intent, int flags, int startId)");
        return super.onStartCommand(intent, flags, startId);
    }



    private void initMediaPlayerFile() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(DatabaseModel.getDatabaseModelInstance(this)
                    .getMusicItemById(playingId).getPath());
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void addMusicStateChangedListener(OnMusicStateChangedListener listener) {
        musicStateChangedListeners.add(listener);
    }

    public void removeMusicStateChangedListener(OnMusicStateChangedListener listener) {
        musicStateChangedListeners.remove(listener);
    }

    private void notifyStateChanged(int playState) {
        Log.d(LOG_TAG, "notifyStateChanged");
        for (OnMusicStateChangedListener listener : musicStateChangedListeners) {
            listener.onMusicStateChanged(playState);
        }
    }


}
