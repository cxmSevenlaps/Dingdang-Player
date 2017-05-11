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

public class MainActivity extends AppCompatActivity{

    private ListView mMusicListView;
    private MusicItemAdapter mMusicItemAdapter;
    private List<MusicItem> mMusicList;
    private Button mBtnDetails;
    private ImageButton mIBtnPlayOrPause;
    private MusicItem mMusicItem;
    private PlayController playController=PlayController.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mMusicListView = (ListView)findViewById(R.id.listview_music_list);
        mMusicList = new ArrayList<MusicItem>();

//        initList(mMusicList);//测试Listview显示使用
        mMusicList = DatabaseModel.getDatabaseModelInstance(this).loadMusic(this);
        mMusicItemAdapter = new MusicItemAdapter(mMusicList, this);
        mMusicListView.setAdapter(mMusicItemAdapter);

        mBtnDetails = (Button) findViewById(R.id.btn_activity_jump_to_details);
        mBtnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MusicDetailsActivity.class);
                intent.putExtra("id", mMusicItem.getmId());

                startActivity(intent);
            }
        });

        mIBtnPlayOrPause = (ImageButton)findViewById(R.id.btn_play_or_pause);
        mIBtnPlayOrPause.setImageResource(R.mipmap.play);
        mIBtnPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playController.getPlayState()== PlayStateConstant.ISPAUSE){
                    playController.setPlayState(PlayStateConstant.ISPLAYING);
                    playController.play();
                    mIBtnPlayOrPause.setImageResource(R.mipmap.pause);
                }else if (PlayController.getInstance().getPlayState()== PlayStateConstant.ISPLAYING){
                    playController.setPlayState(PlayStateConstant.ISPAUSE);
                    playController.getInstance().pause();
                    mIBtnPlayOrPause.setImageResource(R.mipmap.play);
                }
            }
        });

        /*跳转到MusicDetailsActivity*/
        mMusicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("MainActivity", "listview click");
                mMusicItem = mMusicItemAdapter.getItem(position);
                performSetBtnDetailsClick();
                Intent intentService = new Intent(MainActivity.this,MusicService.class);
                intentService.putExtra("id", mMusicItem.getmId());
                startService(intentService);
//                performMusicListItemClick(mMusicItem);
//                Log.d("MainActivity", mMusicItem.getmId()+"");
            }
        });

    }

    /**
     * btn上显示正在播放的歌曲信息,设置播放图标
     */
    private void performSetBtnDetailsClick(){
        mBtnDetails.setText("正在播放:"+mMusicItem.getmMusicTitle());
        mIBtnPlayOrPause.setImageResource(R.mipmap.pause);
    }

    private void performMusicListItemClick(MusicItem item){
        Log.d("MainActivity", "id: "+item.getmId());

        Intent intent = new Intent(MainActivity.this, MusicDetailsActivity.class);
        intent.putExtra("id", item.getmId());

        startActivity(intent);
    }


    /*测试Listview显示使用*/
//    private void initList(List<MusicItem> musicList){
//        MusicItem item = new MusicItem("忘情水", "刘德华");
//        for (int i = 0; i<40; i++){
//            musicList.add(item);
//        }
//    }


}
