package com.example.sevenlaps.dingdangplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.sevenlaps.controller.PlayStateConstant;
import com.example.sevenlaps.orm.DatabaseModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener, MusicService.OnMusicStateChangedListener {
    private static final String LOG_TAG = "MainActivity";
    private ListView mMusicListView;
    private MusicItemAdapter mMusicItemAdapter;
    private List<MusicItem> mMusicList;
    private Button mBtnDetails;
    private ImageButton mIBtnPlayOrPause;
    private MusicItem mMusicItem;

    private MusicService mBoundService;
    private MusicService.MusicBinder mMusicBinder;
    private boolean mIsBound = false;    //是否绑定服务
    private Intent mServiceIntent;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "onServiceConnected");
            mMusicBinder = (MusicService.MusicBinder) service;
            mBoundService = (mMusicBinder).getService();
            mBoundService.addMusicStateChangedListener(MainActivity.this);
            mBoundService.setmNumberOfSongs(mMusicItemAdapter.getCount());
            mBoundService.setmFrontActivityId(0);

            /*第一次打开app,默认加载列表第一首歌*/
            mBoundService.setPath(DatabaseModel.getDatabaseModelInstance(MainActivity.this)
                    .getMusicItemById(1).getPath());
            mBoundService.setPlayingId(1);

            updateView(mBoundService.getPlayState());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, "onServiceDisconnected");
            mBoundService = null;
        }
    };

    void doBindService() {
        Log.d(LOG_TAG, "doBindService()");
        if (bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE)) {
            mIsBound = true;
        }
    }

    void doUnbindService() {
        Log.d(LOG_TAG, "doUnbindService()");
        if (mIsBound == true) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        mServiceIntent = new Intent(MainActivity.this, MusicService.class);
        doBindService();
    }

    private void initView() {

        mMusicListView = (ListView) findViewById(R.id.listview_music_list);
        mBtnDetails = (Button) findViewById(R.id.btn_activity_jump_to_details);
        mIBtnPlayOrPause = (ImageButton) findViewById(R.id.btn_play_or_pause);
        mBtnDetails.setOnClickListener(this);
        mIBtnPlayOrPause.setOnClickListener(this);
        mMusicListView.setOnItemClickListener(this);
        mIBtnPlayOrPause.setImageResource(R.mipmap.play);

        mMusicList = new ArrayList<MusicItem>();
        mMusicList = DatabaseModel.getDatabaseModelInstance(this).loadMusic(this);
        mMusicItemAdapter = new MusicItemAdapter(mMusicList, this);
        mMusicListView.setAdapter(mMusicItemAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_activity_jump_to_details:
                performBtnJumpToDetailsClick();
                break;
            case R.id.btn_play_or_pause:
                performBtnPlayOrPauseClick();
                break;
            default:
                break;
        }
    }

    private void performBtnPlayOrPauseClick() {
        mBoundService.playOrPause();
    }

    private void performBtnJumpToDetailsClick() {
        Log.d(LOG_TAG, "performBtnJumpToDetailsClick");
        mBoundService.setmFrontActivityId(1);//点击通知,跳转到details页面
        Intent intent = new Intent(MainActivity.this, MusicDetailsActivity.class);
        intent.putExtra("id", mBoundService.getPlayingId());

        startActivity(intent);

    }

    /**
     * btn上显示正在播放的歌曲信息,设置播放图标
     */
    private void updateButtonUI(int playState) {
        MusicItem item;
        item = DatabaseModel.getDatabaseModelInstance(this).getMusicItemById(mBoundService.getPlayingId());
        if (item.getMusicTitle() == null) {
            mBtnDetails.setText("歌曲名无法显示");
        } else {
            mBtnDetails.setText("正在播放:" + item.getMusicTitle());
        }

        switch (playState) {
            case PlayStateConstant.ISPLAYING:
                mIBtnPlayOrPause.setImageResource(R.mipmap.pause);
                break;
            case PlayStateConstant.IS_STOP:
                mIBtnPlayOrPause.setImageResource(R.mipmap.play);
                break;
            case PlayStateConstant.ISPAUSE:

                mIBtnPlayOrPause.setImageResource(R.mipmap.play);
                break;
            default:
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("MainActivity", "listview click");

        performItemClick(position);

    }


    private void performItemClick(int position) {

        Log.d(LOG_TAG, "performItemClick");
        mMusicItem = DatabaseModel.getDatabaseModelInstance(this)
                .getMusicItemById(mMusicItemAdapter.getItem(position).getmId());
        int id = mMusicItemAdapter.getItem(position).getmId();//选中列表中的歌曲,把id传到playMusic里面去
        mBoundService.listClickMusicPlay(id);

//
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop()");
        super.onStop();
//        doUnbindService();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();
        mBoundService.stopMusic();
        doUnbindService();

    }

    @Override
    protected void onResume() {

        Log.d(LOG_TAG, "onResume");
        super.onResume();

    }

    @Override
    protected void onRestart() {
        Log.d(LOG_TAG,"onRestart()");
        mBoundService.setmFrontActivityId(0);
        super.onRestart();
//        doBindService();
    }

    @Override
    public void onMusicStateChanged(int playState) {
        Log.d(LOG_TAG, "onMusicStateChanged");
        updateView(mBoundService.getPlayState());
    }

    private void updateView(int playState) {
        updateButtonUI(playState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(LOG_TAG,"onNewIntent(Intent intent)");
        super.onNewIntent(intent);

//        int messageType = getIntent().getIntExtra("message", 0);
        switch (mBoundService.getmFrontActivityId()){
            case 0:
                //啥也不做
                break;
            case 1:
                Intent intentToDetails = new Intent(this, MusicDetailsActivity.class);
                intentToDetails.putExtra("id", mBoundService.getPlayingId());
                startActivity(intentToDetails);
                break;
            default:
                break;
        }
    }
}






