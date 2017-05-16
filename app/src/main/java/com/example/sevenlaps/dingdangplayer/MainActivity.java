package com.example.sevenlaps.dingdangplayer;

import android.content.Intent;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private ListView mMusicListView;
    private MusicItemAdapter mMusicItemAdapter;
    private List<MusicItem> mMusicList;
    private Button mBtnDetails;
    private ImageButton mIBtnPlayOrPause;
    private MusicItem mMusicItem;
    private PlayController playController = PlayController.getInstance();
    private String mCurrentPlayingPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mMusicListView = (ListView) findViewById(R.id.listview_music_list);
        mMusicList = new ArrayList<MusicItem>();

//        initList(mMusicList);//测试Listview显示使用
        mMusicList = DatabaseModel.getDatabaseModelInstance(this).loadMusic(this);
        mMusicItemAdapter = new MusicItemAdapter(mMusicList, this);
        mMusicListView.setAdapter(mMusicItemAdapter);

        mBtnDetails = (Button) findViewById(R.id.btn_activity_jump_to_details);
        mBtnDetails.setOnClickListener(this);

        mIBtnPlayOrPause = (ImageButton) findViewById(R.id.btn_play_or_pause);
        mIBtnPlayOrPause.setImageResource(R.mipmap.play);
        mIBtnPlayOrPause.setOnClickListener(this);

        /*跳转到MusicDetailsActivity*/
        mMusicListView.setOnItemClickListener(this);

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
        if (playController.getPlayState() == PlayStateConstant.ISPAUSE) {
            playController.play();
            playController.setPlayState(PlayStateConstant.ISPLAYING);

            mIBtnPlayOrPause.setImageResource(R.mipmap.pause);
        } else if (PlayController.getInstance().getPlayState() == PlayStateConstant.ISPLAYING) {
            playController.setPlayState(PlayStateConstant.ISPAUSE);
            playController.getInstance().pause();
            mIBtnPlayOrPause.setImageResource(R.mipmap.play);
        }
    }

    private void performBtnJumpToDetailsClick() {
        Intent intent = new Intent(MainActivity.this, MusicDetailsActivity.class);
        intent.putExtra("id", mMusicItem.getmId());

        startActivity(intent);
    }

    /**
     * btn上显示正在播放的歌曲信息,设置播放图标
     */
    private void updateButtonUI() {
        MusicItem item;
        item = DatabaseModel.getDatabaseModelInstance(this).getMusicItemById(playController.getIsPlayingId());
        if (item.getmMusicTitle() == null) {
            mBtnDetails.setText("歌曲名无法显示");
        } else {
            mBtnDetails.setText("正在播放:" + item.getmMusicTitle());
        }

        switch (playController.getPlayState()) {
            case PlayStateConstant.ISPLAYING:
            case PlayStateConstant.IS_STOP:
                mIBtnPlayOrPause.setImageResource(R.mipmap.pause);
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
        mMusicItem = mMusicItemAdapter.getItem(position);
        mMusicItem.setPath(DatabaseModel.getDatabaseModelInstance(this).getMusicItemById(mMusicItem.getmId()).getPath());
        Log.d("MainActivity", "mCurrentPlayingPath:" + mCurrentPlayingPath);
        Log.d("MainActivity", "mMusicItem.getPath():" + mMusicItem.getPath());
        if ((mCurrentPlayingPath == null) ||
                (mMusicItem.getPath().compareTo(mCurrentPlayingPath) != 0)) {//第一次打开app播放||选择非"正在播放"的歌曲

            playController.destroy();
            mCurrentPlayingPath = mMusicItem.getPath();
            playController.setIsPlayingId(mMusicItem.getmId());
            Intent intentService = new Intent(MainActivity.this, MusicService.class);
            intentService.putExtra("id", mMusicItem.getmId());
            startService(intentService);
            updateButtonUI();
        } else {
            if (playController.getPlayState() == PlayStateConstant.ISPAUSE) {//如果是处于暂停状态,那么点击歌曲列表同一首歌,则继续播放
                playController.play();
                playController.setPlayState(PlayStateConstant.ISPLAYING);

                mIBtnPlayOrPause.setImageResource(R.mipmap.pause);
            } else {
                Log.d("MainActivity", "点击同一首歌:" + mMusicItem.getmMusicTitle() + ",于是啥也不干");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playController.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateButtonUI();
    }
}


    /*测试Listview显示使用*/
//    private void initList(List<MusicItem> musicList){
//        MusicItem item = new MusicItem("忘情水", "刘德华");
//        for (int i = 0; i<40; i++){
//            musicList.add(item);
//        }
//    }



