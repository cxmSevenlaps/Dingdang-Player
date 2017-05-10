package com.example.sevenlaps.orm;

import android.content.Context;
import android.os.Environment;

import com.example.sevenlaps.dingdangplayer.MusicItem;
import com.example.sevenlaps.loader.MusicLoader;

import java.util.List;

/**
 * Created by 7laps on 2017/5/7.
 * 数据库管理类
 */

public class DatabaseModel {
    private static final String MUSIC_DIR= Environment.getExternalStorageDirectory().getAbsolutePath()+"/qqmusic/song";
    private MusicDao mMusicDao;
    private static DatabaseModel databaseModelInstance;

    public DatabaseModel(Context context) {
        mMusicDao = new MusicDaoImplement(context);
    }

    public static DatabaseModel getDatabaseModelInstance(Context context){
        if (null== databaseModelInstance){
            databaseModelInstance = new DatabaseModel(context);
        }
        return databaseModelInstance;
    }

    public List<MusicItem> loadMusic(Context context){
        List<MusicItem> items = null;
        //如果数据库里面有,就load数据库里的
        items = loadMusicDataFromDatabase();
        if (items.isEmpty()){
            //如果数据库里面没有,就查sd卡
//            items = new MusicLoader().getMusicDataFromSDCard(context);
            items = new MusicLoader().loadMusicListFromSDCard(MUSIC_DIR);
            //查完就赶紧更新数据库
            mMusicDao.insertItems(items);
        }
         return items;
    }

    public List<MusicItem> loadMusicDataFromDatabase() {
        //更新数据库数据

        return mMusicDao.getAll();
    }

    public void updateMusicDatabase(List<MusicItem> items){
        mMusicDao.insertItems(items);
    }
}
