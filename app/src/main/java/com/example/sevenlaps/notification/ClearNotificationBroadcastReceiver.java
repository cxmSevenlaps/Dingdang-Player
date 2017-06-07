package com.example.sevenlaps.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.sevenlaps.dingdangplayer.MainActivity;
import com.example.sevenlaps.utils.ActivityContainer;

public class ClearNotificationBroadcastReceiver extends BroadcastReceiver {
    public ClearNotificationBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        int notificationId = intent.getIntExtra(DingdangNotificationHelper.KEY_NOTICE_ID, -1);
        if (notificationId!=-1){
            DingdangNotificationHelper.clearNotification(context, notificationId);
            ActivityContainer.getContainer().finishAllActivities();
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
//http://www.sixwolf.net/blog/2016/04/18/Android%E8%87%AA%E5%AE%9A%E4%B9%89Notification%E5%B9%B6%E6%B2%A1%E6%9C%89%E9%82%A3%E4%B9%88%E7%AE%80%E5%8D%95/