package com.example.sevenlaps.dingdangplayer;

/**
 * Created by 7laps on 2017/5/7.
 */

public class MusicItem {
    public int mId;
    private String mMusicTitle;
    private String mArtist;

    public MusicItem(){}

    public MusicItem(int id) {
        this.mId = id;
    }

    public MusicItem(String mMusicTitle, String mArtist) {
        this.mMusicTitle = mMusicTitle;
        this.mArtist = mArtist;
    }

    public String getmMusicTitle() {
        return mMusicTitle;
    }

    public void setmMusicTitle(String mMusicTitle) {
        this.mMusicTitle = mMusicTitle;
    }

    public String getmArtist() {
        return mArtist;
    }

    public void setmArtist(String mArtist) {
        this.mArtist = mArtist;
    }

    public int getmId() {
        return mId;
    }

}
