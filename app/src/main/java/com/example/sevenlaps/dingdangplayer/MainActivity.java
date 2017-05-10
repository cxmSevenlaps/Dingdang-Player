package com.example.sevenlaps.dingdangplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.sevenlaps.orm.DatabaseModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mMusicListView;
    private MusicItemAdapter mMusicItemAdapter;
    private List<MusicItem> mMusicList;

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

        /*跳转到MusicDetailsActivity*/
        mMusicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("MainActivity", "listview click");
                MusicItem item = mMusicItemAdapter.getItem(position);
                Log.d("MainActivity", item.getmId()+"");
                performMusicListItemClick(item);
            }
        });

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
