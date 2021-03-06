package com.example.sevenlaps.dingdangplayer;

import android.content.Context;
import android.util.Log;
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

    public MusicItemAdapter(List<MusicItem> mMusicList, Context context) {
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

        if (null == convertView) {
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.music_item, null);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.music_title);
            holder.tvArtist = (TextView) convertView.findViewById(R.id.music_artist);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if((mMusicList.get(position).getMusicTitle() == null)
                &&(mMusicList.get(position).getmArtist() == null)){
            holder.tvArtist.setText("未知艺术家");
            holder.tvTitle.setText("未知歌曲");
        }else if (mMusicList.get(position).getMusicTitle() == null) {
            holder.tvTitle.setText("未知歌曲");
            holder.tvArtist.setText(mMusicList.get(position).getmArtist());
        }else if(mMusicList.get(position).getmArtist() == null){
            holder.tvArtist.setText("未知艺术家");
            holder.tvTitle.setText(mMusicList.get(position).getMusicTitle());
        }else{
            holder.tvTitle.setText(mMusicList.get(position).getMusicTitle());
            holder.tvArtist.setText(mMusicList.get(position).getmArtist());
        }
        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public MusicItem getItem(int position) {
        Log.d("MusicItemAdapter", "position=" + position);
        return mMusicList.get(position);
    }

    private final class ViewHolder {
        public TextView tvTitle;
        public TextView tvArtist;
    }
}
