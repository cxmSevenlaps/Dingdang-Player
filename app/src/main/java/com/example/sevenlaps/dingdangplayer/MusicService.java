package com.example.sevenlaps.dingdangplayer;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.sevenlaps.controller.PlayModeConstant;
import com.example.sevenlaps.controller.PlayStateConstant;
import com.example.sevenlaps.notification.DingdangReceiver;
import com.example.sevenlaps.notification.NotificationHelper;
import com.example.sevenlaps.orm.DatabaseModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    private final int FIRST_SONG_ID = 1;//列表中第一首歌的ID
    DingdangReceiver mReceiver;
    /**
     * 监听播放模式
     *
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(LOG_TAG, "onCompletion");
        playNext();
    }

    public interface OnMusicStateChangedListener {
        public void onMusicStateChanged(int playState);
    }

    private final int NOTIFICATION_DINGDANG_MUSIC = R.string.dingdang_music_service_started;
    private List<OnMusicStateChangedListener> musicStateChangedListeners;
    private int playState = PlayStateConstant.IS_STOP;//播放状态
    private int playingId = 1;//正在播放的歌曲id,默认第一首
    private String path;
    private int mNumberOfSongs = 0;
    private int mFrontActivityId = 0;//标识那个Activity在最前面, 0是MainActivity, 1是DetailsActivity

    private int mPlayMode = PlayModeConstant.PLAYMODE_SEQUENTIAL;

    public void setmPlayMode(int mPlayMode) {
        this.mPlayMode = mPlayMode;
    }

    public int getmPlayMode() {
        return mPlayMode;
    }

    private static final String LOG_TAG = "MusicService";
    private final IBinder musicBinder = new MusicBinder();
    private MediaPlayer mMediaPlayer = new MediaPlayer();

    private NotificationManager notificationManager;

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

    public int getmFrontActivityId() {
        Log.d(LOG_TAG, " getmFrontActivityId() ,mFrontActivityId= " + mFrontActivityId);
        return mFrontActivityId;
    }

    public void setmFrontActivityId(int mFrontActivityId) {
        Log.d(LOG_TAG, " setmFrontActivityId() ,mFrontActivityId= " + mFrontActivityId);
        this.mFrontActivityId = mFrontActivityId;
    }

    public MediaPlayer getmMediaPlayer() {
        return mMediaPlayer;
    }

    public void setmMediaPlayer(MediaPlayer mMediaPlayer) {
        this.mMediaPlayer = mMediaPlayer;
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

    public int getmNumberOfSongs() {
        return mNumberOfSongs;
    }

    public void setmNumberOfSongs(int mNumberOfSongs) {
        this.mNumberOfSongs = mNumberOfSongs;
    }

    /**
     * 用于列表选取歌曲播放
     *
     * @param id 从列表中选中的歌曲的ID
     */
//    public void playMusic(MusicItem item) {
    public void listClickMusicPlay(int id) {

        Log.d(LOG_TAG, "playMusic(int id)");
        switch (getPlayState()) {
            case PlayStateConstant.ISPLAYING:
            case PlayStateConstant.ISPAUSE:
                if (id != playingId) {
                    playingId = id;
                    Log.d(LOG_TAG, "mediaplayer reset:换歌");
                    initMediaPlayerFile();
                    playMusic();

                } else {   //在列表中点击同一首歌
                    if (playState == PlayStateConstant.ISPLAYING) {
                        Log.d(LOG_TAG, "已经在播放: " + DatabaseModel.getDatabaseModelInstance(this)
                                .getMusicItemById(playingId).getMusicTitle() + ",因此啥也不用做");
                    } else if (playState == PlayStateConstant.ISPAUSE) {//如果是暂停状态,点击后继续播放
                        Log.d(LOG_TAG, "继续播放: " + DatabaseModel.getDatabaseModelInstance(this)
                                .getMusicItemById(playingId).getMusicTitle());
                        playMusic();
                    }
                }
                break;
            case PlayStateConstant.IS_STOP://首次打开app
                playingId = id;
                initMediaPlayerFile();
                playMusic();
                break;
            default:
                break;
        }

        playState = PlayStateConstant.ISPLAYING;
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
                playMusic();
                break;
            case PlayStateConstant.IS_STOP://首次打开app,点击播放
                initMediaPlayerFile();
                playMusic();
                setPlayState(PlayStateConstant.ISPLAYING);
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
            mMediaPlayer.setOnCompletionListener(this);//设置监听，歌曲结束时候的动作
            setPlayState(PlayStateConstant.ISPLAYING);
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

    /**
     * 播放下一曲（根据播放模式）
     */
    public void playNext() {
        Log.d(LOG_TAG, "playNext()");
        switch (mPlayMode) {
            case PlayModeConstant.PLAYMODE_SEQUENTIAL:
                if (playingId == mNumberOfSongs) {
                    playingId = FIRST_SONG_ID;//如果是最后一首了,下一曲设置为第一首

                } else {
                    playingId++;
                }
                break;
            case PlayModeConstant.PLAYMODE_RANDOM:
                do {
                    playingId = new Random().nextInt(mNumberOfSongs);
                }while (playingId==0);
                break;
            case PlayModeConstant.PLAYMODE_SINGLE_CYCLE:
                //playingId不变
                break;
            default:
                break;

        }

        initMediaPlayerFile();
        playMusic();
        setPlayState(PlayStateConstant.ISPLAYING);
        notifyStateChanged(playState);
    }

    public void playPrevious() {
        Log.d(LOG_TAG, "playPrevious()");

        switch (mPlayMode) {
            case PlayModeConstant.PLAYMODE_SEQUENTIAL:
                if (playingId == 1) {
                    playingId = mNumberOfSongs;//如果是第一首,上一曲设置为列表最后一首
                } else {
                    playingId--;
                }
                break;
            case PlayModeConstant.PLAYMODE_RANDOM://歌曲是从1开始的
                do {
                    playingId = new Random().nextInt(mNumberOfSongs);
                }while (playingId==0);
                break;
            case PlayModeConstant.PLAYMODE_SINGLE_CYCLE:
                //playingId不变
                break;
            default:
                break;

        }
        initMediaPlayerFile();
        playMusic();
        setPlayState(PlayStateConstant.ISPLAYING);
        notifyStateChanged(playState);
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
//        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        showNotification();

        registerDingdangReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand(Intent intent, int flags, int startId)");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();

        if (mReceiver!=null){
            unregisterReceiver(mReceiver);
        }
//        notificationManager.cancel(NOTIFICATION_DINGDANG_MUSIC);
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

    private void registerDingdangReceiver(){
        mReceiver = new DingdangReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NotificationHelper.ACTION_PLAYNEXT);
        filter.addAction(NotificationHelper.ACTION_CLOSE_NOTICE);
        filter.addAction(NotificationHelper.ACTION_PLAY_OR_PAUSE);

        registerReceiver(mReceiver, filter);
    }
}
