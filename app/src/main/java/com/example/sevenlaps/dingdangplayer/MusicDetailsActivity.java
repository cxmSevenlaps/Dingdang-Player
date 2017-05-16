package com.example.sevenlaps.dingdangplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.sevenlaps.controller.PlayController;
import com.example.sevenlaps.controller.PlayStateConstant;
import com.example.sevenlaps.orm.DatabaseModel;

public class MusicDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mTextViewArtist;
    private TextView mTextViewTitle;
    private int mMusicId;
    private MusicItem mMusicItem;
    private static SeekBar mSeekBar;

    private ImageButton mIbtnPlayOrPause;

    private PlayController playController = PlayController.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_details);

        mTextViewTitle = (TextView) findViewById(R.id.tv_title);
        mTextViewArtist = (TextView) findViewById(R.id.tv_artist);

        Intent intent = getIntent();
        mMusicId = intent.getIntExtra("id", -2);
        Log.d("MusicDetailsActivity", mMusicId + "");
        if (mMusicId == -2) {//不是从Mainactivity跳转过来的
            finish();
            return;
        }
        mMusicItem = DatabaseModel.getDatabaseModelInstance(this).getMusicItemById(mMusicId);
        if (null == mMusicItem) {
            Log.d("MusicDetailsActivity", "mMusicItem is null");
            return;
        }
        Log.d("MusicDetailsActivity", mMusicItem.getmArtist() + "--" + mMusicItem.getmMusicTitle());
        mTextViewArtist.setText(mMusicItem.getmArtist());
        mTextViewTitle.setText(mMusicItem.getmMusicTitle());

        /*seekbar*/
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {//手指抬起
                int progress = seekBar.getProgress();//拖动进度条,获取进度信息
                playController.seekTo(progress);

            }
        });

        mIbtnPlayOrPause = (ImageButton) findViewById(R.id.ibtn_details_play_or_pause);
        if (playController.getPlayState() == PlayStateConstant.ISPAUSE) {
            mIbtnPlayOrPause.setImageResource(R.mipmap.play);
        } else if (playController.getPlayState() == PlayStateConstant.ISPLAYING) {

            mIbtnPlayOrPause.setImageResource(R.mipmap.pause);
        }
        mIbtnPlayOrPause.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_details_play_or_pause:
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

            mIbtnPlayOrPause.setImageResource(R.mipmap.pause);
        } else if (PlayController.getInstance().getPlayState() == PlayStateConstant.ISPLAYING) {
            playController.setPlayState(PlayStateConstant.ISPAUSE);
            playController.getInstance().pause();
            mIbtnPlayOrPause.setImageResource(R.mipmap.play);
        }
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
