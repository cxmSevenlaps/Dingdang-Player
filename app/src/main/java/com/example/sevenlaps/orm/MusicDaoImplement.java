package com.example.sevenlaps.orm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.sevenlaps.dingdangplayer.MusicItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 7laps on 2017/5/7.
 */

public class MusicDaoImplement implements MusicDao {
    private static final String LOG_TAG = "MUSICE_DAO_IMPLEMENT";
    private static final String MUSIC_DATABASE_NAME = "Music.db";
    private static final String MUSIC_INFO_TABLE = "MusicInfo";
    private static final int VERSION = 1;

    private DatabaseHelper mDbHelper;

    public MusicDaoImplement(Context context) {
        mDbHelper = new DatabaseHelper(context, MUSIC_DATABASE_NAME, null, VERSION);
    }

    @Override
    public void insert(MusicItem item) {
        ContentValues cv = new ContentValues();
        cv.put("title", item.getMusicTitle());
        cv.put("artist", item.getmArtist());
        cv.put("path", item.getPath());
        cv.put("duration", item.getDuration());
        cv.put("artwork", item.getmArtWork());
        getMusicInfoDatabase().insert(MUSIC_INFO_TABLE, null, cv);
    }

    @Override
    public void insertItems(List<MusicItem> items) {
        ContentValues cv = new ContentValues();
        for (MusicItem item : items) {
            cv.put("title", item.getMusicTitle());
            cv.put("artist", item.getmArtist());
            cv.put("path", item.getPath());
            cv.put("duration", item.getDuration());
            cv.put("artwork", item.getmArtWork());
            if (item.ismFavorite()){
                cv.put("favorite", 1);
            }else {
                cv.put("favorite", 0);
            }

            getMusicInfoDatabase().insert(MUSIC_INFO_TABLE, null, cv);
            Log.d(LOG_TAG, "insert " + item.getMusicTitle() + "path" + item.getPath() + " to database");
            Log.d(LOG_TAG, "favorite: "+item.ismFavorite());
            cv.clear();
        }
        Log.d(LOG_TAG, "insert items to database");
    }

    @Override
    public void delete(MusicItem item) {
        getMusicInfoDatabase().delete(MUSIC_INFO_TABLE, "id = ?", new String[]{"" + item.getmId()});
    }

    @Override
    public void deleteItems(List<MusicItem> items) {
        for (MusicItem item : items) {
            getMusicInfoDatabase().delete(MUSIC_INFO_TABLE, "id=?", new String[]{"" + item.getmId()});
        }
    }

    @Override
    public MusicItem getItemById(int id) {
        MusicItem item = new MusicItem();
        Cursor cursor = getMusicInfoDatabase().query(MUSIC_INFO_TABLE, null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
//        Cursor cursor = getMusicInfoDatabase().rawQuery("select * from "+MUSIC_INFO_TABLE+" where id = ? ", new String[]{String.valueOf(id)});
        Log.d("MusicDaoImplement", "get in MusicDaoImplement");
        if (cursor.moveToFirst()) {

            item.setmId(cursor.getInt(cursor.getColumnIndex("id")));
            item.setMusicTitle(cursor.getString(cursor.getColumnIndex("title")));
            item.setmArtist(cursor.getString(cursor.getColumnIndex("artist")));
            item.setPath(cursor.getString(cursor.getColumnIndex("path")));
            item.setDuration(cursor.getString(cursor.getColumnIndex("duration")));
            item.setmArtWork(cursor.getBlob(cursor.getColumnIndex("artwork")));
            Log.d("MusicDaoImplement", "" + "id=" + item.getmId()
                    + " title: " + item.getMusicTitle()
                    + " path:" + item.getPath()
                    + " duration:" + item.getDuration());
        }
        cursor.close();
        return item;
    }

    @Override
    public List<MusicItem> getAll() {
        List<MusicItem> items = new ArrayList<>();

        Cursor cursor = getMusicInfoDatabase().query(MUSIC_INFO_TABLE, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                MusicItem item = new MusicItem();
                item.setmId(cursor.getInt(cursor.getColumnIndex("id")));
                item.setMusicTitle(cursor.getString(cursor.getColumnIndex("title")));
                item.setmArtist(cursor.getString(cursor.getColumnIndex("artist")));
                item.setDuration(cursor.getString(cursor.getColumnIndex("duration")));
                items.add(item);
                Log.d(LOG_TAG, "add to list:  title: " + item.getMusicTitle() + " artist: " + item.getmArtist() + " id=" + item.getmId());
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(LOG_TAG, "items size = " + items.size());
        for (MusicItem musicItem : items) {
            Log.d(LOG_TAG, "title: " + musicItem.getMusicTitle() + " artist: " + musicItem.getmArtist());
        }
        return items;
    }

    @Override
    public int getItemsQuantity() {
        int itemsQuantity = 0;
        Cursor cursor = getMusicInfoDatabase().query(MUSIC_INFO_TABLE, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                itemsQuantity++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return itemsQuantity;
    }


    /*新建数据库,并获取数据库对象*/
    private SQLiteDatabase getMusicInfoDatabase() {
        return mDbHelper.getWritableDatabase();
    }
}
