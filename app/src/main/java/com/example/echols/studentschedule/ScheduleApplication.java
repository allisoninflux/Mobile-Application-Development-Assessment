package com.example.echols.studentschedule;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * This is mainly needed to run LeakCanary to watch for memory leaks
 * https://github.com/square/leakcanary
 */
public class ScheduleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }
}
