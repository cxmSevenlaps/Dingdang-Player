package com.example.sevenlaps.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import com.example.sevenlaps.utils.ActivityContainer;

public class DingdangReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "ClearNotificationBR";

    public DingdangReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d(LOG_TAG, "onReceive(Context context, Intent intent)");
        String action = intent.getAction();
        int notificationId = intent.getIntExtra(NotificationHelper.KEY_NOTICE_ID, -1);
        int notificationCategory = intent.getIntExtra(NotificationHelper.NOTIFICATION_CATEGORY, -1);
        if ((notificationId==-1)||(notificationCategory==-1)||(action.isEmpty())){//说明有错误，退出app
//            NotificationHelper.clearNotification(context, notificationId);
//            ActivityContainer.getContainer().finishAllActivities();
        }

        switch (notificationCategory){
            case NotificationHelper.CLOSE_NOTICE:
                NotificationHelper.clearNotification(context, notificationId);
                ActivityContainer.getContainer().finishAllActivities();
                break;
            case NotificationHelper.PLAY_OR_PAUSE:
                NotificationHelper.playOrPause();//问题出在这里,拔出耳机后,代码会跑到这里
                break;
            case NotificationHelper.PLAY_NEXT:
                NotificationHelper.playNext();
                break;
            default:break;
        }

        if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)){//拔出耳机
            NotificationHelper.pauseMusic();
        }else if (action.equals(AudioManager.ACTION_HEADSET_PLUG)){
//            NotificationHelper.playMusic();
        }

//        throw new UnsupportedOperationException("Not yet implemented");
    }


}
