package com.example.sevenlaps.controller;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
/**
 * Created by 7laps on 2017/5/10.
 */

public class PlayController {
    private static final String PLAY_CONTROLLER_LOG="PlayController";
    private int playState=PlayStateConstant.IS_STOP;
    private MediaPlayer mediaPlayer;
    private Timer mTimer;

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private String path;

    private static final PlayController playControllerInstance = new PlayController();


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
        if (playState==PlayStateConstant.IS_STOP) {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(playControllerInstance.getPath());
                Log.d(PLAY_CONTROLLER_LOG, "path:" + playControllerInstance.getPath());
//            mediaPlayer.setAudioAttributes(AudioAttributes.CONTENT_TYPE_MUSIC);
            mediaPlayer.prepare();
//                mediaPlayer.prepareAsync();
//                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                    @Override
//                    public void onPrepared(MediaPlayer mp) {
//                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
////                        addTimer();
                            playControllerInstance.setPlayState(PlayStateConstant.ISPLAYING);

//                        }
//                    }
//                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(playState==PlayStateConstant.ISPAUSE){
            mediaPlayer.start();
            playControllerInstance.setPlayState(PlayStateConstant.ISPLAYING);
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
        Log.d(PLAY_CONTROLLER_LOG, "destroy()");
        if (mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            playControllerInstance.setPlayState(PlayStateConstant.IS_STOP);
        }

        if(mTimer!=null){
            mTimer.cancel();
            mTimer=null;
        }
    }


    public void playNext(){

    }

    public void seekTo(int progress){
        mediaPlayer.seekTo(progress);
    }

//    public void addTimer(){
//        if(mTimer == null){
//            mTimer = new Timer();//timer就是开启子线程执行任务，与纯粹的子线程不同的是可以控制子线城执行的时间，
//            mTimer.schedule(new TimerTask() {
//
//                @Override
//                public void run() {
//                    //获取歌曲总时长
//                    int duration = mediaPlayer.getDuration();
//                    //获取歌曲当前播放进度
//                    int currentPosition= mediaPlayer.getCurrentPosition();
//                    Message msg = MusicDetailsActivity.handler.obtainMessage();
//                    //把进度封装至消息对象中
//                    Bundle bundle = new Bundle();
//                    bundle.putInt("duration", duration);
//                    bundle.putInt("currentPosition", currentPosition);
//                    msg.setData(bundle);
//                    MusicDetailsActivity.handler.sendMessage(msg);
//                }
//                //开始计时任务后的5毫秒后第一次执行run方法，以后每500毫秒执行一次
//            }, 5, 500);
//        }
//    }


}
