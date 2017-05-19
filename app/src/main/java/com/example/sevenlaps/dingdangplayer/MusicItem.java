package com.example.sevenlaps.dingdangplayer;

/**
 * Created by 7laps on 2017/5/7.
 */

public class MusicItem {

    private int mId;  //歌曲在数据库里面的ID，播放歌曲时候获取歌曲信息都靠传这个ID
    private String musicTitle;
    private String mArtist;
    private String path;
    private String duration;

    public MusicItem(){}

//    public MusicItem(int id) {
//        this.mId = id;
//    }

//    public MusicItem(String mMusicTitle, String mArtist) {
//        this.mMusicTitle = mMusicTitle;
//        this.mArtist = mArtist;
//    }


    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getMusicTitle() {
        return musicTitle;
    }

    public void setMusicTitle(String musicTitle) {
        this.musicTitle = musicTitle;
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

    public void setmId(int mId) {
        this.mId = mId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
