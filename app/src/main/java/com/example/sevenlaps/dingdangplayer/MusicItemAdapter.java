package com.example.sevenlaps.dingdangplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 7laps on 2017/5/7.
 */

public class MusicItemAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<MusicItem> mMusicList;

    public MusicItemAdapter(List<MusicItem> mMusicList, Context context){
        this.mInflater = LayoutInflater.from(context);
        this.mMusicList = mMusicList;
    }
    @Override
    public int getCount() {
        return mMusicList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (null==convertView){
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.music_item,null);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.music_title);
            holder.tvArtist = (TextView)convertView.findViewById(R.id.music_artist);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.tvTitle.setText(mMusicList.get(position).getmMusicTitle());
        holder.tvArtist.setText(mMusicList.get(position).getmArtist());

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    private final class ViewHolder{
        public TextView tvTitle;
        public TextView tvArtist;
    }
}
