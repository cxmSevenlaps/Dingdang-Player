package com.example.sevenlaps.dingdangplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.sevenlaps.controller.PlayStateConstant;
import com.example.sevenlaps.orm.DatabaseModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener{
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
        Log.d(LOG_TAG, " getmFrontActivityId() ,mFrontActivityId= "+mFrontActivityId);
        return mFrontActivityId;
    }

    public void setmFrontActivityId(int mFrontActivityId) {
        Log.d(LOG_TAG, " setmFrontActivityId() ,mFrontActivityId= "+mFrontActivityId);
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
            mMediaPlayer.setOnCompletionListener(this);
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

    public void playNext() {
        Log.d(LOG_TAG, "playNext()");
        if (playingId == mNumberOfSongs) {
            playingId = 1;//如果是最后一首了,下一曲设置为第一首

        } else {
            playingId++;
        }
        initMediaPlayerFile();
        playMusic();
        setPlayState(PlayStateConstant.ISPLAYING);
        notifyStateChanged(playState);
    }

    public void playPrevious() {
        Log.d(LOG_TAG, "playPrevious()");
        if (playingId == 1) {
            playingId = mNumberOfSongs;//如果是第一首,上一曲设置为列表最后一首
        } else {
            playingId--;
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
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();
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
        notificationManager.cancel(NOTIFICATION_DINGDANG_MUSIC);
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

    private void showNotification() {
        Log.d(LOG_TAG, "showNotification");
        Intent intent=new Intent(this, MainActivity.class);

//        intent.putExtra("message", mFrontActivityId);
//        intent.putExtra("message", 1);
//        switch (mFrontActivityId) {
//            case 0:
//                intent = new Intent(this, MainActivity.class);
//                break;
//            case 1:
//                intent = new Intent(this, MusicDetailsActivity.class);
//                break;
//            default:
//                break;
//        }

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent, 0);
//        PendingIntent pendingIntent = PendingIntent
//                .getActivities(this, 0, makeIntentStack(this), 0);
        MusicItem item = DatabaseModel.getDatabaseModelInstance(this)
                .getMusicItemById(playingId);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(item.getMusicTitle())
                .setWhen(System.currentTimeMillis())
                .setContentTitle(item.getMusicTitle())
                .setContentText(item.getmArtist())
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(NOTIFICATION_DINGDANG_MUSIC, notification);
    }

    private Intent[] makeIntentStack(Context context){
        Log.d(LOG_TAG, "makeIntentStack(Context context)");
        Intent[] intents = new Intent[2];
        intents[0] = Intent.makeRestartActivityTask(new ComponentName(context, MainActivity.class));
        intents[1] = new Intent(context, MusicDetailsActivity.class);

        return intents;
    }
}
