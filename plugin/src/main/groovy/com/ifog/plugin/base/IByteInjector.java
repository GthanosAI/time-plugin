package com.ifog.plugin.base;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author: created by hewei
 * @date: 2019/5/17
 * Copyright (c) 2019 https://www.gthanos.cn. All rights reserved.
 */

public interface IByteInjector {

    /**
     * 判断是否需要字节注入
     *
     * @param className
     * @return
     */
    boolean isInjectorClass(String className);

    /**
     * 将文件流转化成字节流
     *
     * @param inputStream
     * @return
     */
    byte[] processClassToByteArray(InputStream inputStream) ;

    /**
     * 将class文件转换成文件
     *
     * @param inputFile    输入文件
     * @param outputFile   输出文件
     * @param inputBaseDir 输入路径
     */
    void processClassFile(File inputFile, File outputFile, String inputBaseDir);


    /**
     * 处理jar包文件
     *
     * @param inputJar
     * @param outputJar
     */
    void processJar(File inputJar, File outputJar) throws IOException;
}
