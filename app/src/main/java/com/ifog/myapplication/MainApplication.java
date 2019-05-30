package com.ifog.myapplication;

import android.app.Application;

import com.ifog.timedebug.TimeDebugerManager;

/**
 * @author: created by hewei
 * @date: 2019/5/26
 * Copyright (c) 2019 https://www.qutoutiao.net. All rights reserved.
 */

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TimeDebugerManager.isDeug = true;
    }
}
