package com.example.sevenlaps.controller;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created by 7laps on 2017/5/10.
 */

public class PlayController extends MediaPlayer {

    public interface OnMusicStateChangedListener {
        public void onMusicStateChanged(int playState);
    }

    private List<OnMusicStateChangedListener> musicStateChangedListeners;


    private static final String PLAY_CONTROLLER_LOG = "PlayController";
    private int playState = PlayStateConstant.IS_STOP;
    private MediaPlayer mediaPlayer;
    private int isPlayingId;  //记录正在播放的歌曲的id
    private int numberOfSongs = 0;//记录ListView中的歌曲数量
    private String path;
    private Timer mTimer;

    public void addMusicStateChangedListener(OnMusicStateChangedListener listener) {
        musicStateChangedListeners.add(listener);
    }
    public void removeMusicStateChangedListener(OnMusicStateChangedListener listener){
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
        musicStateChangedListeners = new ArrayList<OnMusicStateChangedListener>();
        this.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

            }
        });
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


    public void play() {
        Log.d(PLAY_CONTROLLER_LOG, "play()");
        if (playState == PlayStateConstant.IS_STOP) {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(playControllerInstance.getPath());
                Log.d(PLAY_CONTROLLER_LOG, "path:" + playControllerInstance.getPath());
                Log.d(PLAY_CONTROLLER_LOG, "playing song id is: " + playControllerInstance.getIsPlayingId());
                mediaPlayer.prepare();


                mediaPlayer.start();
                playControllerInstance.setPlayState(PlayStateConstant.ISPLAYING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (playState == PlayStateConstant.ISPAUSE) {//如果歌曲处于暂停状态，就不去重新设定mediaPlayer
            mediaPlayer.start();
            playControllerInstance.setPlayState(PlayStateConstant.ISPLAYING);
        }else if (playState == PlayStateConstant.ISPLAYING){
            mediaPlayer.pause();
            playControllerInstance.setPlayState(PlayStateConstant.ISPAUSE);
        }

        notifyStateChanged(playControllerInstance.getPlayState());
    }

    public void pause() {
        Log.d(PLAY_CONTROLLER_LOG, "pause()");
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playControllerInstance.setPlayState(PlayStateConstant.ISPAUSE);
        }
    }


    public void destroy() {
        Log.d(PLAY_CONTROLLER_LOG, "destroy()");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            playControllerInstance.setPlayState(PlayStateConstant.IS_STOP);
        }

    }


    public void seekTo(int progress) {
        mediaPlayer.seekTo(progress);
    }

    /**
     * 通知页面更新
     */
    private void notifyStateChanged(int playState){
        Log.d(PLAY_CONTROLLER_LOG, "notifyStateChanged");
        for (OnMusicStateChangedListener listener:musicStateChangedListeners){
            listener.onMusicStateChanged(playState);
        }
    }
}
