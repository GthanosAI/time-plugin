package com.ifog.timedebug;

import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

/**
 * @author: created by hewei
 * @date: 2019/5/17
 */

public class TimeDebugerManager {

    public static boolean isDeug = true;

    private static ITimeLogger logger;

    public static void setLogger(ITimeLogger logger) {
        TimeDebugerManager.logger = logger;
    }

    //call in plugin
    public static void timeMethod(String method, long time) {
        if (isMainThread()) {
            if (isDeug) {
                Log.d("xxxx", "method:" + method + ", time:" + time);
            }
            if (logger != null) {
                logger.logger(method, time);
            }
        }
    }

    private static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }
}
