package com.example.sevenlaps.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.sevenlaps.dingdangplayer.MainActivity;
import com.example.sevenlaps.utils.ActivityContainer;

public class ClearNotificationBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "ClearNotificationBR";
    public ClearNotificationBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d(LOG_TAG, "onReceive(Context context, Intent intent)");
        int notificationId = intent.getIntExtra(DingdangNotificationHelper.KEY_NOTICE_ID, -1);
        int notificationCategory = intent.getIntExtra(DingdangNotificationHelper.NOTIFICATION_CATEGORY, -1);
        if ((notificationId==-1)||(notificationCategory==-1)){//说明有错误，退出app
            DingdangNotificationHelper.clearNotification(context, notificationId);
            ActivityContainer.getContainer().finishAllActivities();
        }

        switch (notificationCategory){
            case DingdangNotificationHelper.CLOSE_NOTICE:
                DingdangNotificationHelper.clearNotification(context, notificationId);
                ActivityContainer.getContainer().finishAllActivities();
                break;
            default:break;
        }
//        throw new UnsupportedOperationException("Not yet implemented");
    }
}
