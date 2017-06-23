package com.example.sevenlaps.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.sevenlaps.controller.PlayStateConstant;
import com.example.sevenlaps.dingdangplayer.MainActivity;
import com.example.sevenlaps.dingdangplayer.MusicItem;
import com.example.sevenlaps.dingdangplayer.MusicService;
import com.example.sevenlaps.dingdangplayer.R;
import com.example.sevenlaps.orm.DatabaseModel;
import com.example.sevenlaps.utils.DingdangApplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by chenxianmin on 2017/6/6.
 */

public class NotificationHelper{
    public static final String KEY_NOTICE_ID = "NOTICE_ID";
    public static final int NOTICE_ID = 0;

    /*通知的种类*/
    public static final String NOTIFICATION_CATEGORY = "NOTIFICATION_CATEGORY";
    public static final int CLOSE_NOTICE = 0;//关闭通知，并退出app
    public static final int PLAY_OR_PAUSE = 1;
    public static final int PLAY_NEXT = 2;

    /*通知的过滤器*/
    public static final String ACTION_CLOSE_NOTICE = "com.example.sevenlaps.notification.action.closenotice";
    public static final String ACTION_PLAY_OR_PAUSE = "com.example.sevenlaps.notification.action.playorpausenotice";
    public static final String ACTION_PLAYNEXT = "com.example.sevenlaps.notification.action.playnext";


    private static final String LOG_TAG = "NotificationHelper";

    public NotificationHelper() {

    }

    public static void sendNotification(Context context, MusicService service) {
        Log.d(LOG_TAG, "sendNotification");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setOngoing(true);//Ongoing notifications do not have an 'X' close button, and are not affected by the "Clear all" button.
//        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_dingdang_notification);
        initRemoteViews(remoteViews,context, service);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(KEY_NOTICE_ID, NOTICE_ID);

        /*跳转到app首页*/
        int requestCode = (int) SystemClock.uptimeMillis();
        PendingIntent pi = PendingIntent
                .getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.dingdang_notification, pi);

        /*关闭通知*/
        int requestCodeClose = (int) SystemClock.uptimeMillis();
        Intent intentCloseNotification = new Intent(ACTION_CLOSE_NOTICE);
        intentCloseNotification.putExtra(KEY_NOTICE_ID, NOTICE_ID);
        intentCloseNotification.putExtra(NOTIFICATION_CATEGORY, CLOSE_NOTICE);
        PendingIntent piCloseNotification = PendingIntent
                .getBroadcast(context,requestCodeClose,intentCloseNotification,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_close_notification, piCloseNotification);

        /*播放/暂停*/
        int requestCodePlayOrPause = (int) SystemClock.uptimeMillis();
        Intent intentPlayOrPauseNotification = new Intent(ACTION_PLAY_OR_PAUSE);
        intentPlayOrPauseNotification.putExtra(KEY_NOTICE_ID, NOTICE_ID);
        intentPlayOrPauseNotification.putExtra(NOTIFICATION_CATEGORY, PLAY_OR_PAUSE);
        PendingIntent piPlayOrPauseNotification = PendingIntent
                .getBroadcast(context,requestCodePlayOrPause,intentPlayOrPauseNotification,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_play_pause, piPlayOrPauseNotification);

        /*下一曲*/
        int requestCodePlayNext = (int) SystemClock.uptimeMillis();
        Intent intentPlayNextNotification = new Intent(ACTION_PLAYNEXT);
        intentPlayNextNotification.putExtra(KEY_NOTICE_ID, NOTICE_ID);
        intentPlayNextNotification.putExtra(NOTIFICATION_CATEGORY, PLAY_NEXT);
        PendingIntent piPlayNextNotification = PendingIntent
                .getBroadcast(context,requestCodePlayNext,intentPlayNextNotification,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_playnext, piPlayNextNotification);

        builder.setSmallIcon(R.mipmap.ic_launcher);
        Notification notification = builder.build();
//        if(android.os.Build.VERSION.SDK_INT >= 16) {
//            notification = builder.build();
//            notification.bigContentView = remoteViews;
//        }
        notification.contentView = remoteViews;
        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTICE_ID, notification);
    }

    private static String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.SIMPLIFIED_CHINESE);
        return format.format(new Date());
    }

    public static void clearNotification(Context context, int notificationId){
        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
    }

    public static void playOrPause(){
        MusicService service = DingdangApplication.getDingdangApplication().getmService();
        service.playOrPause();
    }

    public static void playNext(){
        MusicService service = DingdangApplication.getDingdangApplication().getmService();
        service.playNext();
    }

    public static void pauseMusic(){
        MusicService service = DingdangApplication.getDingdangApplication().getmService();
        service.pauseMusic();
    }

    public static void playMusic(){
        if (DingdangApplication.getDingdangApplication().ismIsBound()==true){

            MusicService service = DingdangApplication.getDingdangApplication().getmService();
            service.playMusic();
        }else {
            //第一次打开app,还未绑定的时候,马上插入耳机
            //do nothing
        }
    }

    private static void initRemoteViews(RemoteViews remoteViews, Context context, MusicService service){
        Log.d(LOG_TAG, "initRemoteViews");
        MusicItem song = DatabaseModel.getDatabaseModelInstance(context).getMusicItemById(service.getPlayingId());
        remoteViews.setTextViewText(R.id.notification_song_title, song.getMusicTitle());
        remoteViews.setTextViewText(R.id.notification_song_artist, song.getmArtist());
        remoteViews.setImageViewResource(R.id.notification_icon, R.mipmap.ic_launcher);
        remoteViews.setImageViewResource(R.id.notification_playnext, R.mipmap.play_next);
//        remoteViews.setTextViewText(R.id.notification_time_tv, getTime());
//        switch (service.getPlayState()){
//            case PlayStateConstant.ISPLAYING:
//                remoteViews.setImageViewResource(R.id.notification_play_pause, R.mipmap.pause);
//                break;
//            case PlayStateConstant.ISPAUSE:
//                remoteViews.setImageViewResource(R.id.notification_play_pause, R.mipmap.play);
//                break;
//            default:
//                break;
//        }
        remoteViews.setImageViewResource(R.id.notification_play_pause,
                service.getPlayState()==PlayStateConstant.ISPLAYING? R.mipmap.pause:R.mipmap.play);
    }
}
