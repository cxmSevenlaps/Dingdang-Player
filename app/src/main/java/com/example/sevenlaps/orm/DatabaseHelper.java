package com.example.sevenlaps.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 7laps on 2017/5/7.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_MUSIC_INFO = "create table MusicInfo ("
            +"id integer primary key autoincrement, "
            +"title text, "
            +"artist text, "
            +"duration text,"
            + "path text)";
    private Context mContext;

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, version);
        mContext = context;
    }




    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MUSIC_INFO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
