package com.ifog.plugin;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

/**
 * 类方法字节注入的实现类
 *
 * @author: created by hewei
 * @date: 2019/5/17
 * Copyright (c) 2019 https://www.gthanos.cn. All rights reserved.
 */

public class TimeDebugMethodVisitor extends LocalVariablesSorter implements Opcodes {
    private int startVarIndex;
    private String methodName;
    private boolean inject = false;

    public TimeDebugMethodVisitor(int api, MethodVisitor mv, int access, String name, String desc) {
        super(api, access, desc, mv);
        this.methodName = name.replace("/", ".");
    }

    public void setInject(boolean inject){
        this.inject = inject;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        if (inject) {
            mv.visitMethodInsn(INVOKESTATIC, Setting.SYSTEM_CLOCK_PACKAGE, Setting.SYSTEM_CLOCK_METHOD, Setting.SYSTEM_CLOCK_METHOD_SIGNATURE, false);
            startVarIndex = newLocal(Type.LONG_TYPE);
            mv.visitVarInsn(Opcodes.LSTORE, startVarIndex);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (Setting.DEBUG_ANNOTATION.equals(desc)) {
            this.inject = true;
        }

        System.out.println("==========:" + desc + "," + visible);

        return super.visitAnnotation(desc, visible);
    }

    @Override
    public void visitInsn(int opcode) {
        if (inject) {
            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                mv.visitMethodInsn(INVOKESTATIC, Setting.SYSTEM_CLOCK_PACKAGE, Setting.SYSTEM_CLOCK_METHOD, Setting.SYSTEM_CLOCK_METHOD_SIGNATURE, false);
                mv.visitVarInsn(LLOAD, startVarIndex);
                mv.visitInsn(LSUB);
                int index = newLocal(Type.LONG_TYPE);
                mv.visitVarInsn(LSTORE, index);
                mv.visitLdcInsn(methodName);
                mv.visitVarInsn(LLOAD, index);
                mv.visitMethodInsn(INVOKESTATIC, Setting.TIMEDEBUGERMANAGER_CLASS, Setting.TIMEDEBUGERMANAGER_METHOD, Setting.TIMEDEBUGERMANAGER_METHOD_SIGNATURE, false);
            }
        }

        super.visitInsn(opcode);
    }

}
