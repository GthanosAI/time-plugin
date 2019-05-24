package com.ifog.plugin;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.TransformInput;
import com.android.build.gradle.AppExtension;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.gradle.api.Project;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

/**
 * @author: created by hewei
 * @date: 2019/5/17
 * Copyright (c) 2019 https://www.gthanos.cn. All rights reserved.
 */

public class ClassLoaderHelper {
    public static URLClassLoader getClassLoader(Collection<TransformInput> inputs,
                                                Collection<TransformInput> referencedInputs,
                                                Project project) throws MalformedURLException {


        ImmutableList.Builder<URL> urls = new ImmutableList.Builder<>();
        String androidJarPath  = getAndroidJarPath(project);
        System.out.println(androidJarPath);
        File file = new File(androidJarPath);
        URL androidJarURL = file.toURI().toURL();
        urls.add(androidJarURL);
        for (TransformInput totalInputs : Iterables.concat(inputs, referencedInputs)) {
            for (DirectoryInput directoryInput : totalInputs.getDirectoryInputs()) {
                if (directoryInput.getFile().isDirectory()) {
                    urls.add(directoryInput.getFile().toURI().toURL());
                }
            }
            for (JarInput jarInput : totalInputs.getJarInputs()) {
                if (jarInput.getFile().isFile()) {
                    urls.add(jarInput.getFile().toURI().toURL());
                }
            }
        }
        ImmutableList<URL> allUrls = urls.build();
        URL[] classLoaderUrls = allUrls.toArray(new URL[allUrls.size()]);
        return new URLClassLoader(classLoaderUrls);
    }

    /**
     * Android/SDK/platforms/android-27/android.jar
     */
    private static String getAndroidJarPath(Project project) {
        AppExtension appExtension = (AppExtension)project.getProperties().get("android");
        String sdkDirectory = appExtension.getSdkDirectory().getAbsolutePath();
        String compileSdkVersion = appExtension.getCompileSdkVersion();
        sdkDirectory = sdkDirectory + File.separator + "platforms" + File.separator;
        return sdkDirectory + compileSdkVersion + File.separator + "android.jar";
    }
}
