package com.ifog.plugin;


import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.util.Arrays;

/**
 * 代码类注入的具体实现类
 *
 * @author: created by hewei
 * @date: 2019/5/17
 * Copyright (c) 2019 https://www.gthanos.cn. All rights reserved.
 */

public class TimeDebugClassVisitor extends ClassVisitor {

    private String className;
    private boolean isITimeLoggerMethod = false;
    private boolean isAnnotation;


    public TimeDebugClassVisitor(ClassVisitor classVisitor, boolean isAnnotation) {
        super(Opcodes.ASM5, classVisitor);
        this.isAnnotation = isAnnotation;
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);

        this.className = name;
        this.isITimeLoggerMethod = Arrays.toString(interfaces).contains(Setting.ITIMELOGGER_CLASS);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

        if (isITimeLoggerMethod) {
            return mv;
        } else if (mv != null) {
            mv = new TimeDebugMethodVisitor(Opcodes.ASM5, mv, access, className + File.separator + name, desc);
            ((TimeDebugMethodVisitor) mv).setInject(!isAnnotation);
        }
        return mv;
    }
}
