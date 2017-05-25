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

import com.example.sevenlaps.controller.PlayController;
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
    private PlayController playController = PlayController.getInstance();
    private String mCurrentPlayingPath = null;
    private int playState=PlayStateConstant.IS_STOP;

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

            //打开app,默认加载第一首歌
            /*modify begin 使用服务替代controller*/
            mBoundService.setPath(DatabaseModel.getDatabaseModelInstance(MainActivity.this)
                    .getMusicItemById(1).getPath());
            mBoundService.setPlayingId(1);
            /*modify end 使用服务替代controller*/


            /*modify begin 使用服务替代controller*/
//            playState=mBoundService.getPlayState();
            updateView(mBoundService.getPlayState());
            /*modify end 使用服务替代controller*/
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            Log.d(LOG_TAG, "onServiceDisconnected");
            mBoundService = null;
        }
    };

    void doBindService() {
        Log.d(LOG_TAG, "doBindService()");
        boolean rst = bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound == true) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        playController.addMusicStateChangedListener(this);

        mMusicListView = (ListView) findViewById(R.id.listview_music_list);
        mMusicList = new ArrayList<MusicItem>();

//        initList(mMusicList);//测试Listview显示使用
        mMusicList = DatabaseModel.getDatabaseModelInstance(this).loadMusic(this);
        mMusicItemAdapter = new MusicItemAdapter(mMusicList, this);
        playController.setNumberOfSongs(mMusicItemAdapter.getCount());//设置歌曲数量给控制器，方便“上一曲”等按钮控制
        mMusicListView.setAdapter(mMusicItemAdapter);

        mServiceIntent = new Intent(MainActivity.this, MusicService.class);
        doBindService();

        //刚打开app,默认加载第一首歌
        /*modify begin 使用服务替代controller*/
//        playController.setPath(DatabaseModel.getDatabaseModelInstance(this).getMusicItemById(1).getPath());
//        playController.setIsPlayingId(1);
        /*modify end 使用服务替代controller*/

        mBtnDetails = (Button) findViewById(R.id.btn_activity_jump_to_details);
        mBtnDetails.setOnClickListener(this);

        mIBtnPlayOrPause = (ImageButton) findViewById(R.id.btn_play_or_pause);
        mIBtnPlayOrPause.setImageResource(R.mipmap.play);
        mIBtnPlayOrPause.setOnClickListener(this);

        /*跳转到MusicDetailsActivity*/
        mMusicListView.setOnItemClickListener(this);

        /*modify begin 使用服务替代controller*/
//        updateView(playController.getPlayState());
//        updateView(mBoundService.getPlayState());
        /*modify end 使用服务替代controller*/

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
        /*modify begin 使用服务替代controller*/
//        playController.playOrPause();
        mBoundService.playOrPause();
        /*modify end 使用服务替代controller*/

    }

    private void performBtnJumpToDetailsClick() {
        /*modify begin 使用服务替代controller*/
//        if (playController.getIsPlayingId() == 0)//防止还没选歌曲时候就点击，然后闪退
//        {
//            return;
//        }
//        Intent intent = new Intent(MainActivity.this, MusicDetailsActivity.class);
//        intent.putExtra("id", playController.getIsPlayingId());
        Log.d(LOG_TAG, "performBtnJumpToDetailsClick");
        Intent intent = new Intent(MainActivity.this, MusicDetailsActivity.class);
        intent.putExtra("id", mBoundService.getPlayingId());
        /*modify end 使用服务替代controller*/

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
        /*modify begin 使用服务替代controller*/
//        playController.play(mMusicItem);
        mBoundService.playMusic(mMusicItem);
        /*modify end 使用服务替代controller*/

//
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();
        /*modify begin 使用服务替代controller*/
//        playController.destroy();
        mBoundService.stopMusic();
        doUnbindService();
        /*modify end 使用服务替代controller*/
    }

    @Override
    protected void onResume() {

        Log.d(LOG_TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        updateButtonUI(playController.getPlayState());
    }

    @Override
    public void onMusicStateChanged(int playState) {
        Log.d(LOG_TAG, "onMusicStateChanged");
        updateView(mBoundService.getPlayState());
    }

    private void updateView(int playState) {
        updateButtonUI(playState);
    }
}






