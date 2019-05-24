package com.ifog.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class TimeDebugPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {
        System.out.println("============================")
        System.out.println("开始")
        System.out.println("============================")
        project.extensions.add(Setting.TIME_EXTENSION, TimeDebugExtension)

        AppExtension appExtension = project.getProperties().get("android")
        appExtension.registerTransform(new TimeDebugTransform(project), Collections.EMPTY_LIST)
    }
}