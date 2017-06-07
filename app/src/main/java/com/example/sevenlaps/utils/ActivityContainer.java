package com.example.sevenlaps.utils;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxianmin on 2017/6/7.
 */

public class ActivityContainer {
    private static final String LOG_TAG = "ActivityContainer";
    private static ActivityContainer container = new ActivityContainer();
    private static List<Activity> activityList = new ArrayList<Activity>();

    public ActivityContainer() {
    }

    public static ActivityContainer getContainer(){
        return container;
    }

    public void addActivity(Activity activity){
        activityList.add(activity);
    }

    public void removeActivity(Activity activity){
        activityList.remove(activity);
    }

    public void finishAllActivities(){
        Log.d(LOG_TAG, "finishAllActivities");
        for (Activity activity: activityList){
            activity.finish();
        }
        activityList.clear();
    }

}
