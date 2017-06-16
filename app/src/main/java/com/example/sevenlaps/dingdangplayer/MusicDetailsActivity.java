package com.example.sevenlaps.dingdangplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.sevenlaps.controller.PlayModeConstant;
import com.example.sevenlaps.controller.PlayStateConstant;
import com.example.sevenlaps.notification.NotificationHelper;
import com.example.sevenlaps.orm.DatabaseModel;
import com.example.sevenlaps.utils.ActivityContainer;
import com.example.sevenlaps.utils.DingdangApplication;

import java.text.SimpleDateFormat;


public class MusicDetailsActivity extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, MusicService.OnMusicStateChangedListener {
    private static final String LOG_TAG = "MusicDetailsActivity";
    private TextView mTextViewArtist;
    private TextView mTextViewTitle;
    private ImageView mImageViewArtWork;
    private TextView mTextViewDuration;
    private TextView mTextViewCurrentTime;
    private int mMusicId;//MainActivity传过来的播放歌曲的ID
    private MusicItem mMusicItem;
    private static SeekBar mSeekBar;
    private SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");

    private ImageButton mIbtnPlayMode;
    private ImageButton mIbtnPlayOrPause;
    private ImageButton mIbtnPlayPrevious;
    private ImageButton mIbtnPlayNext;
    private Button mBtnSetFavorite;

    private Handler mHandler = null;
    private boolean isChanging = false;//互斥变量，防止定时器与SeekBar拖动时进度冲突;true说明用户正在拖动还没放手

    private MusicService.MusicBinder mMusicBinder;
    private MusicService mBoundService;
    private boolean mIsBound = false;
    private Intent mServiceIntent;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "onServiceConnected(ComponentName name, IBinder service) ");
            mMusicBinder = (MusicService.MusicBinder) service;
            mBoundService = mMusicBinder.getService();
            mBoundService.addMusicStateChangedListener(MusicDetailsActivity.this);


            /*存起来当做全局控制播放用，其他的地方可以调用*/
            DingdangApplication.getDingdangApplication().setmService(mBoundService);
            DingdangApplication.getDingdangApplication().setmIsBound(true);

            /*设定更新seekbar ui的任务*/
            mHandler = new Handler();
            mHandler.post(mRunnable);
            mMusicItem = DatabaseModel.getDatabaseModelInstance(MusicDetailsActivity.this)
                    .getMusicItemById(mBoundService.getPlayingId());
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

        Log.d(LOG_TAG, "onCreate(Bundle savedInstanceState) ");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_details);

        //从MainActivity或者通知栏的intent从通知栏传过来
        Intent intent = getIntent();
        mMusicId = intent.getIntExtra("id", -2);
        Log.d(LOG_TAG, "mMusicId = " + mMusicId);
        if (mMusicId == -2) {//不是从Mainactivity跳转过来的
            finish();
            return;
        }

        initView();

        mServiceIntent = new Intent(this, MusicService.class);
        doBindService();

        ActivityContainer.getContainer().addActivity(this);

    }

    /**
     * 初始化界面,和所播放的歌曲数据无关的初始化都放在这里。即播哪首歌都要做的事情
     */
    private void initView() {
        mTextViewTitle = (TextView) findViewById(R.id.tv_title);
        mTextViewArtist = (TextView) findViewById(R.id.tv_artist);
        mImageViewArtWork = (ImageView) findViewById(R.id.iv_art_wrok);
        mTextViewDuration = (TextView) findViewById(R.id.tv_duration);
        mTextViewCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        mIbtnPlayMode = (ImageButton) findViewById(R.id.ibtn_playmode);
        mIbtnPlayOrPause = (ImageButton) findViewById(R.id.ibtn_details_play_or_pause);
        mIbtnPlayPrevious = (ImageButton) findViewById(R.id.ibtn_details_play_previous);
        mIbtnPlayNext = (ImageButton) findViewById(R.id.ibtn_details_play_next);
        mBtnSetFavorite = (Button)findViewById(R.id.btn_favorite);

        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);

        mIbtnPlayMode.setOnClickListener(this);
        mIbtnPlayOrPause.setOnClickListener(this);
        mIbtnPlayPrevious.setOnClickListener(this);
        mIbtnPlayNext.setOnClickListener(this);
        mBtnSetFavorite.setOnClickListener(this);

//        mIbtnPlayMode.setImageResource(R.id.);

        mIbtnPlayPrevious.setImageResource(R.mipmap.play_previous);
        mIbtnPlayNext.setImageResource(R.mipmap.play_next);
        mIbtnPlayOrPause.setImageResource(R.mipmap.play);//初始化,什么都没选,直接点到details页面的话显示播放图片

//        setArtWork();
    }

    /**
     * 更新专辑封面
     */
    private void updateArtWork() {
        Log.d(LOG_TAG, "setArtWork()");
        mMusicItem = DatabaseModel.getDatabaseModelInstance(this)
                .getMusicItemById(mBoundService.getPlayingId());
        byte[] artWork = mMusicItem.getmArtWork();
        if (artWork != null) {
            Bitmap bitmap = BitmapFactory
                    .decodeByteArray(mMusicItem.getmArtWork(), 0, mMusicItem.getmArtWork().length);
            mImageViewArtWork.setImageBitmap(bitmap);
        } else {
            mImageViewArtWork.setImageResource(R.mipmap.ic_launcher);
        }
    }

    /**
     * 更新歌曲总时长
     */
    private void updateMaxTextView() {
        Log.d(LOG_TAG, "updateDurationTextView");
        mMusicItem = DatabaseModel.getDatabaseModelInstance(this)
                .getMusicItemById(mBoundService.getPlayingId());
        if (null == mMusicItem) {
            Log.d(LOG_TAG, "mMusicItem is null");
            return;
        }
        Log.d(LOG_TAG, mMusicItem.getmArtist() + "--" + mMusicItem.getMusicTitle());
        long duration = Long.parseLong(mMusicItem.getDuration());
        mTextViewDuration.setText(sdf.format(duration));
    }

    /**
     *更新歌曲名显示文本框
     */
    private void updateTitleTextView() {
        Log.d(LOG_TAG, "updateTitleTextView()");
        mMusicItem = DatabaseModel.getDatabaseModelInstance(this)
                .getMusicItemById(mBoundService.getPlayingId());
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

    /**
     * 更新歌手信息显示框
     */
    private void updateArtistTextView() {
        Log.d(LOG_TAG, "updateArtistTextView()");
        mMusicItem = DatabaseModel.getDatabaseModelInstance(this)
                .getMusicItemById(mBoundService.getPlayingId());
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
            case R.id.ibtn_playmode:
                selectPlayMode();
                break;
            case R.id.ibtn_details_play_or_pause:
                performBtnPlayOrPauseClick();

                break;
            case R.id.ibtn_details_play_previous:
                performBtnPlayPrevious();
                break;
            case R.id.ibtn_details_play_next:
                performBtnPlayNext();
                break;
            case R.id.btn_favorite:
                performSetFavorite();
                break;
            default:
                break;
        }
    }

    /**
     * 添加收藏
     */
    private void performSetFavorite(){
        Log.d(LOG_TAG, "performSetFavorite()");
        //更新数据库的favorite字段

    }
    /**
     * 设置播放模式
     */
    private void selectPlayMode() {
        int playMode = mBoundService.getmPlayMode();
        switch (playMode) {
            case PlayModeConstant.PLAYMODE_SEQUENTIAL:
                mBoundService.setmPlayMode(PlayModeConstant.PLAYMODE_RANDOM);
                updatePlayModeBtnImage();
                break;
            case PlayModeConstant.PLAYMODE_RANDOM:
                mBoundService.setmPlayMode(PlayModeConstant.PLAYMODE_SINGLE_CYCLE);
                updatePlayModeBtnImage();
                break;
            case PlayModeConstant.PLAYMODE_SINGLE_CYCLE:
                mBoundService.setmPlayMode(PlayModeConstant.PLAYMODE_SEQUENTIAL);
                updatePlayModeBtnImage();
                break;
            default:
                break;
        }
    }

    /**
     * 点击播放下一曲
     */
    private void performBtnPlayNext() {
        mBoundService.playNext();
    }

    /**
     * 点击播放上一曲
     */
    private void performBtnPlayPrevious() {
        mBoundService.playPrevious();
    }

    /**
     * 点击"播放/暂停"按钮
     */
    private void performBtnPlayOrPauseClick() {
        mBoundService.playOrPause();
    }


    @Override
    /**
     * 回调,更新界面
     */
    public void onMusicStateChanged(int playState) {
        Log.d(LOG_TAG, "onMusicStateChanged");
        updateView(playState);
//        mBoundService.updateNotification();//更新通知栏
        NotificationHelper.sendNotification(this,mBoundService);
    }

    /**
     * 更新整个界面
     *
     * @param playState
     */
    private void updateView(int playState) {
        mSeekBar.setMax(mBoundService.getmMediaPlayer().getDuration());
        updatePlayModeBtnImage();
        updateBtnPlayOrPauseImage(playState);
        updateTitleTextView();
        updateArtistTextView();
        updateMaxTextView();
        updateArtWork();
    }

    private void updatePlayModeBtnImage(){
        switch (mBoundService.getmPlayMode()) {
            case PlayModeConstant.PLAYMODE_SEQUENTIAL:
                mIbtnPlayMode.setImageResource(R.mipmap.cycle);
                break;
            case PlayModeConstant.PLAYMODE_RANDOM:
                mIbtnPlayMode.setImageResource(R.mipmap.random);
                break;
            case PlayModeConstant.PLAYMODE_SINGLE_CYCLE:
                mIbtnPlayMode.setImageResource(R.mipmap.singlecycle);
                break;
            default:
                break;
        }
    }
    /**
     * 更新"播放/暂停"按钮图标
     *
     * @param playState 播放状态
     */
    private void updateBtnPlayOrPauseImage(int playState) {
        if (playState == PlayStateConstant.ISPLAYING) {
            mIbtnPlayOrPause.setImageResource(R.mipmap.pause);
        } else if (playState == PlayStateConstant.ISPAUSE) {
            mIbtnPlayOrPause.setImageResource(R.mipmap.play);
        }
    }

    /**
     * 歌曲进度条progressBar的回调
     * @param seekBar
     * @param progress
     * @param fromUser
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {//判断是否来自用户的拖动操作
            mTextViewCurrentTime.setText(sdf.format(seekBar.getProgress()));
        }
    }
    /*拖动进度条的时候，进度条不用自动更新*/
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isChanging = true;
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mBoundService.getmMediaPlayer().seekTo(seekBar.getProgress());
        mBoundService.playMusic();
        updateBtnPlayOrPauseImage(mBoundService.getPlayState());
        isChanging = false;
    }

    /**
     * 更新UI的runnable
     */
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (isChanging == false) {//用户在拖动进度条的时候,音乐继续播放,进度条不自动更新
                mSeekBar.setProgress(mBoundService.getmMediaPlayer().getCurrentPosition());
                mTextViewCurrentTime.setText(sdf.format(mBoundService.getmMediaPlayer().getCurrentPosition()));
            }
            mHandler.postDelayed(mRunnable, 1000);
        }
    };

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed()");
        super.onBackPressed();
        mBoundService.setmFrontActivityId(0);
        doUnBindService();
        mHandler.removeCallbacks(mRunnable);

    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy() ");
        doUnBindService();
        mHandler.removeCallbacks(mRunnable);
//        mBoundService.setmFrontActivityId(0);
        super.onDestroy();

        ActivityContainer.getContainer().removeActivity(this);
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "onResume()");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop()");
        mBoundService.setmFrontActivityId(1);
        super.onStop();
    }
}
