package com.ifog.plugin;

import com.ifog.plugin.base.BaseByteInjector;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * @author: created by hewei
 * @date: 2019/5/17
 * Copyright (c) 2019 https://www.qutoutiao.net. All rights reserved.
 */

public class TimeDebugInjector extends BaseByteInjector {

    private TimeDebugExtension extension;

    public boolean isInjectorClass(String className) {
        boolean superResult = super.isInjectorClass(className);
        boolean isByteCodePlugin = className.startsWith(Setting.PLUGIN_LIBRARY);
        if(extension != null) {
            if(!extension.whiteList.isEmpty()) {
                boolean inWhiteList = false;
                for(String item : extension.whiteList) {
                    if(className.startsWith(item)) {
                        inWhiteList = true;
                    }
                }
                return superResult && !isByteCodePlugin && inWhiteList;
            }
        }
        return superResult && !isByteCodePlugin;
    }

    public void setExtension(TimeDebugExtension extension) {
        this.extension = extension;
    }

    @Override
    public ClassVisitor wrapClassWriter(ClassWriter classWriter) {
        return new TimeDebugClassVisitor(classWriter);
    }
}
