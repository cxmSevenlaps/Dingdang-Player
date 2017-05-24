package com.example.sevenlaps.dingdangplayer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

public class MusicDetailsActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, PlayController.OnMusicStateChangedListener {
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
        playController = PlayController.getInstance();
        playController.addMusicStateChangedListener(this);



        /*seekbar*/
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(playController.getMusicDuration());
        mHandler = new Handler();
        mHandler.post(mRunnable);


        mIbtnPlayOrPause = (ImageButton) findViewById(R.id.ibtn_details_play_or_pause);

        mIbtnPlayOrPause.setOnClickListener(this);
        mIbtnPlayPrevious = (ImageButton) findViewById(R.id.ibtn_details_play_previous);
        mIbtnPlayPrevious.setImageResource(R.mipmap.play_previous);
        mIbtnPlayPrevious.setOnClickListener(this);
        mIbtnPlayNext = (ImageButton) findViewById(R.id.ibtn_details_play_next);
        mIbtnPlayNext.setImageResource(R.mipmap.play_next);
        mIbtnPlayNext.setOnClickListener(this);

        updateView(playController.getPlayState());

    }

    private void updateDurationTextView() {
        mMusicItem = DatabaseModel.getDatabaseModelInstance(this).getMusicItemById(playController.getIsPlayingId());
        if (null == mMusicItem) {
            Log.d("MusicDetailsActivity", "mMusicItem is null");
            return;
        }
        Log.d("MusicDetailsActivity", mMusicItem.getmArtist() + "--" + mMusicItem.getMusicTitle());
        long duration = Long.parseLong(mMusicItem.getDuration());
        mTextViewDuration.setText(sdf.format(duration));
    }

    private void updateTitleTextView() {
        mMusicItem = DatabaseModel.getDatabaseModelInstance(this).getMusicItemById(playController.getIsPlayingId());
        if (null == mMusicItem) {
            Log.d("MusicDetailsActivity", "mMusicItem is null");
            return;
        }
        Log.d("MusicDetailsActivity", mMusicItem.getmArtist() + "--" + mMusicItem.getMusicTitle());

        if (mMusicItem.getMusicTitle() == null) {
            mTextViewTitle.setText("--未知歌曲--");
        } else {
            mTextViewTitle.setText("－" + mMusicItem.getMusicTitle() + "－");
        }
    }

    private void updateArtistTextView() {
        mMusicItem = DatabaseModel.getDatabaseModelInstance(this).getMusicItemById(playController.getIsPlayingId());
        if (null == mMusicItem) {
            Log.d("MusicDetailsActivity", "mMusicItem is null");
            return;
        }
        Log.d("MusicDetailsActivity", mMusicItem.getmArtist() + "--" + mMusicItem.getMusicTitle());

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

        if (playController.getIsPlayingId() == playController.getNumberOfSongs()) {
            playController.play(DatabaseModel
                    .getDatabaseModelInstance(this).getMusicItemById(1));
        } else {

            playController.play(DatabaseModel
                    .getDatabaseModelInstance(this).getMusicItemById(playController.getIsPlayingId() + 1));
        }


    }

    /**
     * 点击播放上一曲
     */
    private void performBtnPlayPrevious() {

        if (playController.getIsPlayingId() == 1) {//第一首歌ID是1，1是起始值
            playController.play(DatabaseModel
                    .getDatabaseModelInstance(this).getMusicItemById(playController.getNumberOfSongs()));
        } else {
            playController.play(DatabaseModel
                    .getDatabaseModelInstance(this).getMusicItemById(playController.getIsPlayingId() - 1));
        }
    }

    private void performBtnPlayOrPauseClick() {
        playController.playOrPause();
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
        playController.seekToPosition(seekBar.getProgress());

        if (playController.getPlayState() == PlayStateConstant.ISPAUSE) {
            playController.play();
        }
        isChanging = false;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (isChanging == false) {
                mSeekBar.setProgress(playController.getCurrentPositionEx());
                mTextViewCurrentTime.setText(sdf.format(playController.getCurrentPositionEx()));
            }
            mHandler.postDelayed(mRunnable, 1000);
        }
    };


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mHandler.removeCallbacks(mRunnable);
    }
}
