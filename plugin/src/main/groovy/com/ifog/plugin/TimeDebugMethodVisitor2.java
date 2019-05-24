package com.ifog.plugin;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.io.File;

/**
 * 类方法字节注入的实现类
 *
 * @author: created by hewei
 * @date: 2019/5/17
 * Copyright (c) 2019 https://www.qutoutiao.net. All rights reserved.
 */

public class TimeDebugMethodVisitor2 extends AdviceAdapter implements Opcodes {
    private String methodName;

    protected TimeDebugMethodVisitor2(int api, MethodVisitor mv, int access, String name, String desc) {
        super(api, mv, access, name, desc);
        this.methodName = name.replace(File.separator, ".");
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return super.visitAnnotation(desc, visible);
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        mv.visitMethodInsn(INVOKESTATIC, Setting.SYSTEM_CLOCK_PACKAGE, Setting.SYSTEM_CLOCK_METHOD, Setting.SYSTEM_CLOCK_METHOD_SIGNATURE, false);

    }

    @Override
    protected void onMethodExit(int opcode) {
        System.out.println("======onMethodExi1t");

        mv.visitLdcInsn(methodName);
        mv.visitMethodInsn(INVOKESTATIC, Setting.SYSTEM_CLOCK_PACKAGE, Setting.SYSTEM_CLOCK_METHOD, Setting.SYSTEM_CLOCK_METHOD_SIGNATURE, false);
        mv.visitMethodInsn(INVOKESTATIC, Setting.TIMEDEBUGERMANAGER_CLASS, Setting.TIMEDEBUGERMANAGER_METHOD, Setting.TIMEDEBUGERMANAGER_METHOD_SIGNATURE, false);
        System.out.println("======onMethodExit2");

        super.onMethodExit(opcode);
    }
}
