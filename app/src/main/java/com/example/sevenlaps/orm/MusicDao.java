package com.example.sevenlaps.orm;

import com.example.sevenlaps.dingdangplayer.MusicItem;

import java.util.List;

/**
 * Created by 7laps on 2017/5/7.
 */

public interface MusicDao {
    public void insert(MusicItem item);

    public void insertItems(List<MusicItem> items);

    public void delete(MusicItem item);

    public void deleteItems(List<MusicItem> items);

    public MusicItem getItemById(int id);

    public List<MusicItem> getAll();

    public int getItemsQuantity();//获取条目的数量

    public void setFavorite(int id, int flag);//设置收藏
}
