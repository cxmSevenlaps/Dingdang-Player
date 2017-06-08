package com.example.sevenlaps.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
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

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by chenxianmin on 2017/6/6.
 */

public class DingdangNotificationHelper{
    public static final String KEY_NOTICE_ID = "NOTICE_ID";
    public static final int NOTICE_ID = 0;

    public static final String NOTIFICATION_CATEGORY = "NOTIFICATION_CATEGORY";
    public static final int CLOSE_NOTICE = 0;
    public static final int PLAY_OR_PAUSE = 1;



    public static final String ACTION_CLOSE_NOTICE = "com.example.sevenlaps.notification.action.closenotice";
    public static final String ACTION_PLAY_OR_PAUSE = "com.example.sevenlaps.notification.action.playorpausenotice";
//    public static final int NOTICE_ID = R.string.app_name;
//    private MusicService mService;
//    RemoteViews mRemoteViews;

    private static final String LOG_TAG = "NotificationHelper";

    private static DingdangNotificationHelper notificationHelper = new DingdangNotificationHelper();


    public DingdangNotificationHelper() {
//        mService = DingdangApplication.getDingdangApplication().getmService();
//        mService.addMusicStateChangedListener(this);
    }

    public static DingdangNotificationHelper getNotificationHelper(){
        return notificationHelper;

    }
    public static void sendNotification(Context context, MusicService service) {
        Log.d(LOG_TAG, "sendNotification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setOngoing(true);//Ongoing notifications do not have an 'X' close button, and are not affected by the "Clear all" button.
//        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_dingdang_notification);
        MusicItem song = DatabaseModel.getDatabaseModelInstance(context).getMusicItemById(service.getPlayingId());
        remoteViews.setTextViewText(R.id.notification_song_title, song.getMusicTitle());
        remoteViews.setTextViewText(R.id.notification_song_artist, song.getmArtist());
        remoteViews.setImageViewResource(R.id.notification_icon, R.mipmap.ic_launcher);
//        remoteViews.setTextViewText(R.id.notification_time_tv, getTime());
        switch (service.getPlayState()){
            case PlayStateConstant.ISPLAYING:
                remoteViews.setImageViewResource(R.id.notification_play_pause, R.mipmap.pause);
                break;
            case PlayStateConstant.ISPAUSE:
                remoteViews.setImageViewResource(R.id.notification_play_pause, R.mipmap.play);
                break;
            default:
                break;
        }


        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(KEY_NOTICE_ID, NOTICE_ID);
        /*FLAG_ACTIVITY_CLEAR_TASK :如果在调用Context.startActivity时传递这个标记，
        将会导致任何用来放置该activity的已经存在的task里面的已经存在的activity先清空，
        然后该activity再在该task中启动，也就是说，这个新启动的activity变为了这个空tas的根activity.
        所有老的activity都结束掉。该标志必须和FLAG_ACTIVITY_NEW_TASK一起使用。*/
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int requestCode = (int) SystemClock.uptimeMillis();
        PendingIntent pi = PendingIntent
                .getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.dingdang_notification, pi);

        /*关闭通知*/
        int requestCode1 = (int) SystemClock.uptimeMillis();
        Intent intentCloseNotification = new Intent(ACTION_CLOSE_NOTICE);
        intentCloseNotification.putExtra(KEY_NOTICE_ID, NOTICE_ID);
        intentCloseNotification.putExtra(NOTIFICATION_CATEGORY, CLOSE_NOTICE);
        PendingIntent piCloseNotification = PendingIntent
                .getBroadcast(context,requestCode1,intentCloseNotification,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_close_notification, piCloseNotification);

        /*播放/暂停*/
        int requestCodePlayOrPause = (int) SystemClock.uptimeMillis();
        Intent intentPlayOrPauseNotification = new Intent(ACTION_PLAY_OR_PAUSE);
        intentPlayOrPauseNotification.putExtra(KEY_NOTICE_ID, NOTICE_ID);
        intentPlayOrPauseNotification.putExtra(NOTIFICATION_CATEGORY, PLAY_OR_PAUSE);
        PendingIntent piPlayOrPauseNotification = PendingIntent
                .getBroadcast(context,requestCodePlayOrPause,intentPlayOrPauseNotification,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_play_pause, piPlayOrPauseNotification);

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

//    @Override
//    public void onMusicStateChanged(int playState) {
//        updatePlayOrPauseBtnImg();
//    }

//    private void updatePlayOrPauseBtnImg() {
//
//    }
}
