package com.ifog.plugin;

/**
 * @author: created by hewei
 * @date: 2019/5/17
 * Copyright (c) 2019 https://www.gthanos.cn. All rights reserved.
 */

public class Setting {
    public static final String SYSTEM_CLOCK_PACKAGE = "android/os/SystemClock";
    public static final String SYSTEM_CLOCK_METHOD = "elapsedRealtime";
    public static final String SYSTEM_CLOCK_METHOD_SIGNATURE = "()J";
    public static final String ITIMELOGGER_CLASS = "com/ifog/timedebug/ITimeLogger";
    public static final String TIMEDEBUGERMANAGER_CLASS = "com/ifog/timedebug/TimeDebugerManager";
    public static final String TIMEDEBUGERMANAGER_METHOD = "timeMethod";
    public static final String TIMEDEBUGERMANAGER_METHOD_SIGNATURE = "(Ljava/lang/String;J)V";

    public static final String TIME_EXTENSION = "timeExt";
    public static final String PLUGIN_LIBRARY = "com.ifog.timedebug";
    public static final String DEBUG_ANNOTATION = "Lcom/ifog/timedebug/DebugTime;";


}
