package com.example.sevenlaps.controller;

import android.media.MediaPlayer;
import android.util.Log;

import com.example.sevenlaps.dingdangplayer.MusicItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created by 7laps on 2017/5/10.
 */

public class PlayController{

    public interface OnMusicStateChangedListener {
        public void onMusicStateChanged(int playState);
    }

    private List<OnMusicStateChangedListener> musicStateChangedListeners;


    private static final String LOG_TAG = "PlayController";
    private int playState = PlayStateConstant.IS_STOP;
    private MediaPlayer mediaPlayer;
    private int isPlayingId = 0;  //记录正在播放的歌曲的id
    private int numberOfSongs = 0;//记录ListView中的歌曲数量
    private String path;
    private Timer mTimer;

    public void addMusicStateChangedListener(OnMusicStateChangedListener listener) {
        musicStateChangedListeners.add(listener);
    }

    public void removeMusicStateChangedListener(OnMusicStateChangedListener listener) {
        musicStateChangedListeners.remove(listener);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public int getNumberOfSongs() {
        return numberOfSongs;
    }

    public void setNumberOfSongs(int numberOfSongs) {
        this.numberOfSongs = numberOfSongs;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getIsPlayingId() {
        return isPlayingId;
    }

    public void setIsPlayingId(int isPlayingId) {
        this.isPlayingId = isPlayingId;
    }

    private static final PlayController playControllerInstance = new PlayController();


    private PlayController() {
        super();
        musicStateChangedListeners = new ArrayList<OnMusicStateChangedListener>();//初始化监听列表

    }

    public int getPlayState() {
        return playState;
    }

    public void setPlayState(int playState) {
        this.playState = playState;
    }

    public static PlayController getInstance() {
        return playControllerInstance;
    }

    /**
     * 播放:可以用在MainActivity的列表点击,也可以用于其他的直接播放场景
     * @param item
     */
    public void play(MusicItem item) {
        Log.d(LOG_TAG, "play(MusicItem item)");

        switch (playState) {
            case PlayStateConstant.ISPLAYING:
            case PlayStateConstant.ISPAUSE:
                if (item.getmId() != isPlayingId) {
                    mediaPlayer.reset();
                    Log.d(LOG_TAG, "mediaplayer reset:换歌");
                    try {
                        mediaPlayer.setDataSource(item.getPath());
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.start();

                } else {   //在列表中点击同一首歌
                    if (playState == PlayStateConstant.ISPLAYING) {
                        Log.d(LOG_TAG, "已经在播放: " + item.getMusicTitle() + ",因此啥也不用做");
                    } else if (playState == PlayStateConstant.ISPAUSE) {//如果是暂停状态,点击后继续播放
                        Log.d(LOG_TAG, "继续播放: " + item.getMusicTitle());
                        mediaPlayer.start();
                    }
                }
                break;
            case PlayStateConstant.IS_STOP://首次打开app
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(item.getPath());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();

                break;

            default:
                break;
        }
        playControllerInstance.setPlayState(PlayStateConstant.ISPLAYING);
        playControllerInstance.setIsPlayingId(item.getmId());
        notifyStateChanged(playControllerInstance.getPlayState());
    }

    /**
     * 用于播放/暂停按钮
     */
    public void playOrPause() {
        Log.d(LOG_TAG, " playOrPause");
        switch (playState) {
            case PlayStateConstant.ISPLAYING:
                mediaPlayer.pause();
                playControllerInstance.setPlayState(PlayStateConstant.ISPAUSE);
                break;
            case PlayStateConstant.ISPAUSE:
                mediaPlayer.start();
                playControllerInstance.setPlayState(PlayStateConstant.ISPLAYING);
                break;
            case PlayStateConstant.IS_STOP://首次打开app,点击播放

                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(playControllerInstance.getPath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    playControllerInstance.setPlayState(PlayStateConstant.ISPLAYING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        notifyStateChanged(playControllerInstance.getPlayState());
    }

    public void play() {
        Log.d(LOG_TAG, "play()");
        if (playState == PlayStateConstant.IS_STOP) {//首次打开app,点击播放
//            try {
//                mediaPlayer = new MediaPlayer();
//                mediaPlayer.setDataSource(playControllerInstance.getPath());
//                Log.d(LOG_TAG, "path:" + playControllerInstance.getPath());
//                Log.d(LOG_TAG, "playing song id is: " + playControllerInstance.getIsPlayingId());
//                mediaPlayer.prepare();
//
//
//                mediaPlayer.start();
//                playControllerInstance.setPlayState(PlayStateConstant.ISPLAYING);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        } else if (playState == PlayStateConstant.ISPAUSE) {//如果歌曲处于暂停状态，就不去重新设定mediaPlayer
            mediaPlayer.start();
            playControllerInstance.setPlayState(PlayStateConstant.ISPLAYING);
        } else if (playState == PlayStateConstant.ISPLAYING) {
            mediaPlayer.pause();
            playControllerInstance.setPlayState(PlayStateConstant.ISPAUSE);
        }

        notifyStateChanged(playControllerInstance.getPlayState());
    }
    public void pause(){
        Log.d(LOG_TAG, "pause()");
        mediaPlayer.pause();
        playControllerInstance.setPlayState(PlayStateConstant.ISPAUSE);
        notifyStateChanged(playControllerInstance.getPlayState());
    }

    public void destroy() {
        Log.d(LOG_TAG, "destroy()");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            playControllerInstance.setPlayState(PlayStateConstant.IS_STOP);
        }

    }


    /**
     *
     * @param progress
     */
    public void seekToPosition(int progress) {
        mediaPlayer.seekTo(progress);
    }

    /**
     *
     * @return
     */
    public int getCurrentPositionEx(){
        return mediaPlayer.getCurrentPosition();
    }

    /**
     *
     * @return
     */
    public int getMusicDuration(){
        return mediaPlayer.getDuration();
    }

    /**
     * 通知页面更新
     */
    private void notifyStateChanged(int playState) {
        Log.d(LOG_TAG, "notifyStateChanged");
        for (OnMusicStateChangedListener listener : musicStateChangedListeners) {
            listener.onMusicStateChanged(playState);
        }
    }
}
