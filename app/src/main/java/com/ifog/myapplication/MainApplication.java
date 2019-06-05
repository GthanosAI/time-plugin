package com.ifog.myapplication;

import android.app.Application;
import android.util.Log;

import com.ifog.timedebug.DebugTime;
import com.ifog.timedebug.ITimeLogger;
import com.ifog.timedebug.TimeDebugerManager;

/**
 * @author: created by hewei
 * @date: 2019/5/26
 * Copyright (c) 2019 https://www.qutoutiao.net. All rights reserved.
 */

public class MainApplication extends Application {

    @DebugTime
    @Override
    public void onCreate() {
        super.onCreate();
        TimeDebugerManager.isDeug = true;

        TimeDebugerManager.setLogger(new ITimeLogger() {
            @Override
            public void logger(String method, long cost) {
                Log.e("=======AntiTime=======", "#" + method + ":cost:" + cost);
            }
        });
    }
}
