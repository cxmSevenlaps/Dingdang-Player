package com.example.sevenlaps.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.util.Log;

import com.example.sevenlaps.dingdangplayer.MusicItem;
import com.example.sevenlaps.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 7laps on 2017/5/7.
 */

public class MusicLoader {
    private static final String FLAC = "flac";
    private MediaMetadataRetriever mMediaMetadataRetriever;


    public MusicLoader() {
        mMediaMetadataRetriever = new MediaMetadataRetriever();
    }

    /**
     *
     * @param parentDir
     * @return
     */
    public List<MusicItem> loadMusicListFromSDCard(final String parentDir){
        List<MusicItem> musicItemList = new ArrayList<>();
        List<File> fileList = FileUtils.getFilesByPathAndSuffix(parentDir, FLAC);
        for (File file:fileList){
            musicItemList.add(loadMusicItemFromMusicFile(file));
        }

        return musicItemList;
    }

    private MusicItem loadMusicItemFromMusicFile(File musicFile){
        mMediaMetadataRetriever.setDataSource(musicFile.getAbsolutePath());
        MusicItem item = new MusicItem();
        item.setmArtist(mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        item.setmMusicTitle(mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));

        Log.d("MusicLoader", ""+item.getmArtist()+"  "+item.getmMusicTitle());
        return item;
    }


    /**
     * 用ContentResolver从整个sd卡中取音乐文件的方法,方法的问题可能在于无法持续监听某个文件夹。
     * @param context
     * @return
     */
    public List<MusicItem> getMusicDataFromSDCard(Context context) {

        List<MusicItem> list = new ArrayList<MusicItem>();
        ContentResolver resolver = context.getContentResolver();
        if (null == resolver) {
            return null;
        }
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (null==cursor){
            return null;
        }

        if (cursor.moveToFirst()){
            do {
                MusicItem item = new MusicItem();
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                if ("<unknown>".equals(artist)){
                    artist = "未知艺术家";
                }
                String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                String subStringMp3 = displayName.substring(displayName.length()-3, displayName.length());
                String subStringFlac = displayName.substring(displayName.length()-4, displayName.length());
                if (subStringFlac.equals("flac")||subStringMp3.equals("mp3")){
                    item.setmMusicTitle(title);
                    item.setmArtist(artist);
                    list.add(item);
                }
            }while (cursor.moveToNext());
        }

        return list;
    }
}
