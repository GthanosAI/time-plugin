package com.ifog.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: created by hewei
 * @date: 2019/5/17
 * Copyright (c) 2019 https://www.qutoutiao.net. All rights reserved.
 */

public class TimeDebugExtension {
    public RunVariant runVariant = RunVariant.DEBUG;
    public List<String> whiteList = new ArrayList<>();

    @Override
    public String toString() {
        return "TimeDebugExtension{" +
                "runVariant=" + runVariant +
                ", whiteList=" + whiteList +
                '}';
    }
}
