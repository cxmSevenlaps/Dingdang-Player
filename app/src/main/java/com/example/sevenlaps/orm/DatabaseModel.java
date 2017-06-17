package com.example.sevenlaps.orm;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.sevenlaps.controller.LoadStateConstant;
import com.example.sevenlaps.dingdangplayer.MainActivity;
import com.example.sevenlaps.dingdangplayer.MusicItem;
import com.example.sevenlaps.loader.MusicLoader;

import java.util.List;

/**
 * Created by 7laps on 2017/5/7.
 * 数据库管理类
 */

public class DatabaseModel {
    private static final String LOG_TAG = "DatabaseModel";
    private static final String MUSIC_DIR = Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/dingdangplayer/song_bak";
    public static final int NOT_FAVORITE = 0;
    public static final int IS_FAVORITE = 1;
    private MusicDao mMusicDao;
    private static DatabaseModel databaseModelInstance;

    public DatabaseModel(Context context) {
        mMusicDao = new MusicDaoImplement(context);
    }

    public static DatabaseModel getDatabaseModelInstance(Context context) {
        if (null == databaseModelInstance) {
            databaseModelInstance = new DatabaseModel(context);
        }
        return databaseModelInstance;
    }

    /**
     * 从数据库加载音乐
     *
     * @param context
     * @return
     */
    public List<MusicItem> loadMusic(Context context) {
        Log.d(LOG_TAG, "loadMusic(Context context)");
        MainActivity.setmLoadState(LoadStateConstant.IS_LOADING);
        List<MusicItem> items = null;
        //如果数据库里面有,就load数据库里的
        items = loadMusicDataFromDatabase();
        if (items.isEmpty()) {
            //如果数据库里面没有,就查sd卡
            items = new MusicLoader().loadMusicListFromSDCard(MUSIC_DIR);
            //查完就赶紧更新数据库
            mMusicDao.insertItems(items);
        }
        items = loadMusicDataFromDatabase();//所有的歌曲数据都从数据库里面取
        MainActivity.setmLoadState(LoadStateConstant.HAS_LOADED);
        return items;
    }


    /**
     * 从数据库里面取出所有的音乐信息
     *
     * @return
     */
    private List<MusicItem> loadMusicDataFromDatabase() {
        //更新数据库数据

        return mMusicDao.getAll();
    }

    public void updateMusicDatabase(List<MusicItem> items) {
        mMusicDao.insertItems(items);
    }

    /**
     * @param id
     * @return
     */
    public MusicItem getMusicItemById(int id) {
        MusicItem musicItem = new MusicItem();
        musicItem = mMusicDao.getItemById(id);
        return musicItem;
    }

    public int getItemsQuantity() {
        int itemsQuantity = 0;

        itemsQuantity = mMusicDao.getItemsQuantity();

        return itemsQuantity;
    }

    public void setFavoriteById(int id){
        Log.d(LOG_TAG, "setFavorite("+ id +")");
        MusicItem musicItem = new MusicItem();
        musicItem = mMusicDao.getItemById(id);
        if (musicItem.getmFavorite()==1) {
            mMusicDao.setFavorite(id, NOT_FAVORITE);
        }else if (musicItem.getmFavorite()==0){
            //如果已经收藏,就做取消收藏
            mMusicDao.setFavorite(id, IS_FAVORITE);
        }
    }
}
