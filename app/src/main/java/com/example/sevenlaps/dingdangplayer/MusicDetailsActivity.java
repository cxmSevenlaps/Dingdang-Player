package com.example.sevenlaps.dingdangplayer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import static com.example.sevenlaps.dingdangplayer.R.mipmap.play;

public class MusicDetailsActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
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
    private boolean isChanging = false;//互斥变量，防止定时器与SeekBar拖动时进度冲突

    private PlayController playController = PlayController.getInstance();

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

        updateTitleTextView();
        updateArtistTextView();
        updateDurationTextView();
        /*seekbar*/
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);

        updateSeekBar();




        mIbtnPlayOrPause = (ImageButton) findViewById(R.id.ibtn_details_play_or_pause);
        initPlayOrPauseBtnImage();
        mIbtnPlayOrPause.setOnClickListener(this);
        mIbtnPlayPrevious = (ImageButton) findViewById(R.id.ibtn_details_play_previous);
        mIbtnPlayPrevious.setImageResource(R.mipmap.play_previous);
        mIbtnPlayPrevious.setOnClickListener(this);
        mIbtnPlayNext = (ImageButton) findViewById(R.id.ibtn_details_play_next);
        mIbtnPlayNext.setImageResource(R.mipmap.play_next);
        mIbtnPlayNext.setOnClickListener(this);

    }

    private void updateDurationTextView() {
        mMusicItem = DatabaseModel.getDatabaseModelInstance(this).getMusicItemById(playController.getIsPlayingId());
        if (null == mMusicItem) {
            Log.d("MusicDetailsActivity", "mMusicItem is null");
            return;
        }
        Log.d("MusicDetailsActivity", mMusicItem.getmArtist() + "--" + mMusicItem.getMusicTitle());
        long duration = Long.parseLong(mMusicItem.getDuration());
//        sdf = new SimpleDateFormat("mm:ss");
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

    private void initPlayOrPauseBtnImage() {
        if (playController.getPlayState() == PlayStateConstant.ISPAUSE) {
            mIbtnPlayOrPause.setImageResource(play);
        } else if (playController.getPlayState() == PlayStateConstant.ISPLAYING) {

            mIbtnPlayOrPause.setImageResource(R.mipmap.pause);
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
                initPlayOrPauseBtnImage();
                break;
            case R.id.ibtn_details_play_next:
//                stopTimer();
                performBtnPlayNext();
                initPlayOrPauseBtnImage();

//                updateSeekBar();
                break;
            default:
                break;
        }
    }

    /**
     * 点击播放下一曲
     */
    private void performBtnPlayNext() {

        playController.destroy();
        playController.setPlayState(PlayStateConstant.IS_STOP);
        Intent intentService = new Intent(MusicDetailsActivity.this, MusicService.class);
        stopService(intentService);


        if (playController.getIsPlayingId() == playController.getNumberOfSongs()) {
            intentService.putExtra("id", 1);//第一首歌ID是1，1是起始值
            playController.setIsPlayingId(1);
        } else {
            intentService.putExtra("id", playController.getIsPlayingId() + 1);
            playController.setIsPlayingId(playController.getIsPlayingId() + 1);
        }

        startService(intentService);
        updateTitleTextView();
        updateArtistTextView();
        updateDurationTextView();

    }

    private void performBtnPlayPrevious() {
        playController.destroy();
        playController.setPlayState(PlayStateConstant.IS_STOP);
        Intent intentService = new Intent(MusicDetailsActivity.this, MusicService.class);
        stopService(intentService);

        if (playController.getIsPlayingId() == 1) {//第一首歌ID是1，1是起始值

            intentService.putExtra("id", playController.getNumberOfSongs());
            playController.setIsPlayingId(playController.getNumberOfSongs());
        } else {

            intentService.putExtra("id", playController.getIsPlayingId() - 1);
            playController.setIsPlayingId(playController.getIsPlayingId() - 1);
        }
        startService(intentService);
        updateTitleTextView();
        updateArtistTextView();
        updateDurationTextView();
    }

    private void performBtnPlayOrPauseClick() {
        if (playController.getPlayState() == PlayStateConstant.ISPAUSE) {
            playController.play();
            playController.setPlayState(PlayStateConstant.ISPLAYING);

            mIbtnPlayOrPause.setImageResource(R.mipmap.pause);
        } else if (PlayController.getInstance().getPlayState() == PlayStateConstant.ISPLAYING) {
            playController.setPlayState(PlayStateConstant.ISPAUSE);
            playController.getInstance().pause();
            mIbtnPlayOrPause.setImageResource(play);
        }
    }

    private void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    sendMessage(UPDATE_SEEKBAR_PROGRESS);
                }
            };
        }

        if ((mTimerTask != null) && (mTimer != null)) {
            mTimer.schedule(mTimerTask, 0, 1000);
        }
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    private void updateSeekBarProgress() {
        mSeekBar.setProgress(playController.getMediaPlayer().getCurrentPosition());
    }

    public void sendMessage(int id) {
        if (mHandler != null) {
            Message msg = Message.obtain(mHandler, id);
            mHandler.sendMessage(msg);
            Log.d("MusicDetailsActivity", "send messages to handler");
        }
    }

    private void updateSeekBar() {

        startTimer();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_SEEKBAR_PROGRESS:
                        mSeekBar.setMax(playController.getMediaPlayer().getDuration());//设置进度条
                        mSeekBar.setProgress(playController.getMediaPlayer().getCurrentPosition());
//                        sdf = new SimpleDateFormat("mm:ss");
                        mTextViewCurrentTime.setText(sdf.format(playController.getMediaPlayer().getCurrentPosition()));

                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    //    public static Handler handler = new Handler(){
//        public void handleMessage(android.os.Message msg){
//            Bundle bundle = msg.getData();
//            int duration = bundle.getInt("duration");
//            int currentDuration = bundle.getInt("currentPosition");
//            mSeekBar.setMax(duration);
//            mSeekBar.setProgress(currentDuration);
//        }
//    };
}
