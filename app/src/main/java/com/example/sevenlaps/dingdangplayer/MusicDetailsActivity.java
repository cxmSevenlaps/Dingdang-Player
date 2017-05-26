package com.example.sevenlaps.dingdangplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.sevenlaps.controller.PlayController;
import com.example.sevenlaps.controller.PlayStateConstant;
import com.example.sevenlaps.orm.DatabaseModel;

import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

public class MusicDetailsActivity extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, MusicService.OnMusicStateChangedListener {
    private static final String LOG_TAG = "MusicDetailsActivity";
    private static final int UPDATE_SEEKBAR_PROGRESS = 0;
    private TextView mTextViewArtist;
    private TextView mTextViewTitle;
    private TextView mTextViewDuration;
    private TextView mTextViewCurrentTime;
    private int mMusicId;
    private MusicItem mMusicItem;
    private static SeekBar mSeekBar;
    private SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");

    private ImageButton mIbtnPlayOrPause;
    private ImageButton mIbtnPlayPrevious;
    private ImageButton mIbtnPlayNext;

    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private Handler mHandler = null;
    private boolean isChanging = false;//互斥变量，防止定时器与SeekBar拖动时进度冲突;true说明正在拖动还没放手

    private PlayController playController = null;

    private MusicService.MusicBinder mMusicBinder;
    private MusicService mBoundService;
    private boolean mIsBound = false;
    private Intent mServiceIntent ;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "onServiceConnected(ComponentName name, IBinder service) ");
            mMusicBinder = (MusicService.MusicBinder) service;
            mBoundService = mMusicBinder.getService();
            mBoundService.addMusicStateChangedListener(MusicDetailsActivity.this);

            mHandler = new Handler();
            mHandler.post(mRunnable);

            updateView(mBoundService.getPlayState());

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, "onServiceDisconnected(ComponentName name)");
            mBoundService = null;
        }
    };

    void doBindService() {
        Log.d(LOG_TAG, "doBindService()");
        if (bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE)) {
            mIsBound = true;
        }

    }

    void doUnBindService() {
        Log.d(LOG_TAG, "doUnBindService()");
        if (mIsBound == true) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_details);


        mTextViewTitle = (TextView) findViewById(R.id.tv_title);
        mTextViewArtist = (TextView) findViewById(R.id.tv_artist);
        mTextViewDuration = (TextView) findViewById(R.id.tv_duration);
        mTextViewCurrentTime = (TextView) findViewById(R.id.tv_current_time);

        Intent intent = getIntent();
        mMusicId = intent.getIntExtra("id", -2);
        Log.d("MusicDetailsActivity", mMusicId + "");
        if (mMusicId == -2) {//不是从Mainactivity跳转过来的
            finish();
            return;
        }

        /*modify begin 使用服务替代controller*/
//        playController = PlayController.getInstance();
//        playController.addMusicStateChangedListener(this);
        /*modify end 使用服务替代controller*/


        /*seekbar*/
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);
        /*modify begin 使用服务替代controller*/
//        mSeekBar.setMax(playController.getMusicDuration());

        /*modify end 使用服务替代controller*/



        mIbtnPlayOrPause = (ImageButton) findViewById(R.id.ibtn_details_play_or_pause);

        mIbtnPlayOrPause.setOnClickListener(this);
        mIbtnPlayPrevious = (ImageButton) findViewById(R.id.ibtn_details_play_previous);
        mIbtnPlayPrevious.setImageResource(R.mipmap.play_previous);
        mIbtnPlayPrevious.setOnClickListener(this);
        mIbtnPlayNext = (ImageButton) findViewById(R.id.ibtn_details_play_next);
        mIbtnPlayNext.setImageResource(R.mipmap.play_next);
        mIbtnPlayNext.setOnClickListener(this);

        /*modify begin 使用服务替代controller*/
//        updateView(playController.getPlayState());
        mServiceIntent = new Intent(this, MusicService.class);
        doBindService();
        /*modify end 使用服务替代controller*/

    }

    private void updateDurationTextView() {
        Log.d(LOG_TAG, "updateDurationTextView");
        /*modify begin 使用服务替代controller*/
        mMusicItem = DatabaseModel.getDatabaseModelInstance(this)
                .getMusicItemById(mBoundService.getPlayingId());
         /*modify end 使用服务替代controller*/
        if (null == mMusicItem) {
            Log.d(LOG_TAG, "mMusicItem is null");
            return;
        }
        Log.d(LOG_TAG, mMusicItem.getmArtist() + "--" + mMusicItem.getMusicTitle());
        long duration = Long.parseLong(mMusicItem.getDuration());
        mTextViewDuration.setText(sdf.format(duration));
    }

    private void updateTitleTextView() {
        Log.d(LOG_TAG, "updateTitleTextView()");
        /*modify begin 使用服务替代controller*/
        mMusicItem = DatabaseModel.getDatabaseModelInstance(this)
                .getMusicItemById(mBoundService.getPlayingId());
         /*modify end 使用服务替代controller*/
        if (null == mMusicItem) {
            Log.d(LOG_TAG, "mMusicItem is null");
            return;
        }
        Log.d(LOG_TAG, mMusicItem.getmArtist() + "--" + mMusicItem.getMusicTitle());

        if (mMusicItem.getMusicTitle() == null) {
            mTextViewTitle.setText("--未知歌曲--");
        } else {
            mTextViewTitle.setText("－" + mMusicItem.getMusicTitle() + "－");
        }
    }

    private void updateArtistTextView() {
        Log.d(LOG_TAG, "updateArtistTextView()");
        /*modify begin 使用服务替代controller*/
        mMusicItem = DatabaseModel.getDatabaseModelInstance(this)
                .getMusicItemById(mBoundService.getPlayingId());
         /*modify end 使用服务替代controller*/
        if (null == mMusicItem) {
            Log.d(LOG_TAG, "mMusicItem is null");
            return;
        }
        Log.d(LOG_TAG, mMusicItem.getmArtist() + "--" + mMusicItem.getMusicTitle());

        if (mMusicItem.getmArtist() == null) {
            mTextViewArtist.setText("--未知艺术家--");
        } else {
            mTextViewArtist.setText("－" + mMusicItem.getmArtist() + "－");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_details_play_or_pause:
                performBtnPlayOrPauseClick();

                break;
            case R.id.ibtn_details_play_previous:
                performBtnPlayPrevious();
                break;
            case R.id.ibtn_details_play_next:
                performBtnPlayNext();
                break;
            default:
                break;
        }
    }

    /**
     * 点击播放下一曲
     */
    private void performBtnPlayNext() {
        /*modify begin 使用服务替代controller*/
        mBoundService.playNext();

//        if (playController.getIsPlayingId() == playController.getNumberOfSongs()) {
//            playController.play(DatabaseModel
//                    .getDatabaseModelInstance(this).getMusicItemById(1));
//        } else {
//
//            playController.play(DatabaseModel
//                    .getDatabaseModelInstance(this).getMusicItemById(playController.getIsPlayingId() + 1));
//        }

        /*modify end 使用服务替代controller*/
    }

    /**
     * 点击播放上一曲
     */
    private void performBtnPlayPrevious() {
        /*modify begin 使用服务替代controller*/
        mBoundService.playPrevious();
//        if (playController.getIsPlayingId() == 1) {//第一首歌ID是1，1是起始值
//            playController.play(DatabaseModel
//                    .getDatabaseModelInstance(this).getMusicItemById(playController.getNumberOfSongs()));
//        } else {
//            playController.play(DatabaseModel
//                    .getDatabaseModelInstance(this).getMusicItemById(playController.getIsPlayingId() - 1));
//        }
        /*modify begin 使用服务替代controller*/
    }

    private void performBtnPlayOrPauseClick() {
        /*modify begin 使用服务替代controller*/
        mBoundService.playOrPause();
//        playController.playOrPause();
        /*modify begin 使用服务替代controller*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    /**
     * 回调,更新界面
     */
    public void onMusicStateChanged(int playState) {
        Log.d(LOG_TAG, "onMusicStateChanged");
        updateView(playState);
    }

    private void updateView(int playState) {
        mSeekBar.setMax(mBoundService.getmMediaPlayer().getDuration());
        updateBtnPlayOrPauseImage(playState);
        updateTitleTextView();
        updateArtistTextView();
        updateDurationTextView();
    }


    private void updateSeekBar() {
        mSeekBar.setProgress(playController.getCurrentPositionEx());
    }

    private void updateBtnPlayOrPauseImage(int playState) {
        if (playState == PlayStateConstant.ISPLAYING) {
            mIbtnPlayOrPause.setImageResource(R.mipmap.pause);
        } else if (playState == PlayStateConstant.ISPAUSE) {
            mIbtnPlayOrPause.setImageResource(R.mipmap.play);
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {//判断是否来自用户的拖动操作

            mTextViewCurrentTime.setText(sdf.format(seekBar.getProgress()));
//            isChanging = false;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isChanging = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        /*modify begin 使用服务替代controller*/
//        playController.seekToPosition(seekBar.getProgress());
        mBoundService.getmMediaPlayer().seekTo(seekBar.getProgress());
        mBoundService.playMusic();
        isChanging=false;

//        if (playController.getPlayState() == PlayStateConstant.ISPAUSE) {
//            playController.play();
//        }
    }
    /*modify end 使用服务替代controller*/

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (isChanging == false) {//用户在拖动进度条的时候,音乐继续播放,进度条不自动更新
                /*modify begin 使用服务替代controller*/
//                mSeekBar.setProgress(playController.getCurrentPositionEx());
//                mTextViewCurrentTime.setText(sdf.format(playController.getCurrentPositionEx()));
                mSeekBar.setProgress(mBoundService.getmMediaPlayer().getCurrentPosition());
                mTextViewCurrentTime.setText(sdf.format(mBoundService.getmMediaPlayer().getCurrentPosition()));
                /*modify end 使用服务替代controller*/
            }
            mHandler.postDelayed(mRunnable, 1000);
        }
    };


    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed()");
        super.onBackPressed();
        doUnBindService();
        mHandler.removeCallbacks(mRunnable);

    }
}
