package com.example.sevenlaps.dingdangplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.sevenlaps.orm.DatabaseModel;

public class MusicDetailsActivity extends AppCompatActivity {
    private TextView mTextViewArtist;
    private TextView mTextViewTitle;
    private int mMusicId;
    private MusicItem mMusicItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_details);

        mTextViewTitle = (TextView) findViewById(R.id.tv_title);
        mTextViewArtist = (TextView) findViewById(R.id.tv_artist);

        Intent intent = getIntent();
        mMusicId = intent.getIntExtra("id", -2);
        Log.d("MusicDetailsActivity", mMusicId+"");
        if (mMusicId==-2){//不是从Mainactivity跳转过来的
            finish();
            return;
        }
        mMusicItem = DatabaseModel.getDatabaseModelInstance(this).getMusicItemById(mMusicId);
        if (null==mMusicItem){
            Log.d("MusicDetailsActivity", "mMusicItem is null");
            return;
        }
        Log.d("MusicDetailsActivity", mMusicItem.getmArtist()+"--"+mMusicItem.getmMusicTitle());
        mTextViewArtist.setText(mMusicItem.getmArtist());
        mTextViewTitle.setText(mMusicItem.getmMusicTitle());
    }
}
