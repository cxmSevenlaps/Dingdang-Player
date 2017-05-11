package com.example.sevenlaps.controller;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * Created by 7laps on 2017/5/10.
 */

public class PlayController {
    private static final String PLAY_CONTROLLER_LOG="PlayController";
    private int playState=PlayStateConstant.ISPAUSE;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private String path;

    private static final PlayController playControllerInstance = new PlayController();
    MediaPlayer mediaPlayer = new MediaPlayer();

    private PlayController() {

    }

    public int getPlayState() {
        return playState;
    }

    public void setPlayState(int playState) {
        this.playState = playState;
    }

    public static PlayController getInstance(){
        return playControllerInstance;
    }


    public void play(){
        Log.d(PLAY_CONTROLLER_LOG, "play()");
        try {
            mediaPlayer.setDataSource(playControllerInstance.getPath());
//            mediaPlayer.setAudioAttributes(AudioAttributes.CONTENT_TYPE_MUSIC);
//            mediaPlayer.prepare();
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        playControllerInstance.setPlayState(PlayStateConstant.ISPLAYING);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void pause(){
        Log.d(PLAY_CONTROLLER_LOG, "pause()");
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            playControllerInstance.setPlayState(PlayStateConstant.ISPAUSE);
        }
    }

//    public void initMediaPlayer(String path){
//        Log.d(PLAY_CONTROLLER_LOG, "initMediaPlayer(String path)"+path);
//        try {
//            mediaPlayer.setDataSource(path);
////            mediaPlayer.setAudioAttributes(AudioAttributes.CONTENT_TYPE_MUSIC);
////            mediaPlayer.prepare();
//            mediaPlayer.prepareAsync();
//            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//    }

    public void destroy(){
        if (mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
