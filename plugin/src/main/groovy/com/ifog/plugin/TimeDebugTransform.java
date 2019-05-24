package com.ifog.plugin;

import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.ifog.plugin.base.BaseByteInjector;
import com.ifog.plugin.base.BaseTransform;

import org.gradle.api.Project;

import java.io.IOException;

/**
 * @author: created by hewei
 * @date: 2019/5/16
 * Copyright (c) 2019 https://www.gthanos.cn. All rights reserved.
 */

public class TimeDebugTransform extends BaseTransform {

    private TimeDebugExtension extension;
    private TimeDebugInjector timeDebugInjector;

    public TimeDebugTransform(Project project) {
        super(project);

    }

    @Override
    protected BaseByteInjector getBaseInjector() {
        timeDebugInjector = new TimeDebugInjector();
        return timeDebugInjector;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        extension = (TimeDebugExtension) project.getExtensions().getByName(Setting.TIME_EXTENSION);
        timeDebugInjector.setExtension(extension);
        super.transform(transformInvocation);
    }

    @Override
    public boolean isCacheable() {
        return super.isCacheable();
    }

    @Override
    protected RunVariant getRunVariant() {
        return extension.runVariant;
    }
}
