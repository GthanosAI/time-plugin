package com.ifog.plugin;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: created by hewei
 * @date: 2019/5/17
 * Copyright (c) 2019 https://www.qutoutiao.net. All rights reserved.
 */

public class ExtensionClassWriter extends ClassWriter implements Opcodes {
    private ClassLoader classLoader;
    private static final String OBJECT = "java/lang/Object";


    public ExtensionClassWriter(ClassLoader classLoader, int flags) {
        super(flags);
        this.classLoader = classLoader;
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        if (type1 == null || type1.equals(OBJECT) || type2 == null || type2.equals(OBJECT)) {
            // object is the root of the class hierarchy.
            return OBJECT;
        }

        if (type1.equals(type2)) {
            // two equal.
            return type1;
        }

        ClassReader classReader1 = getClassReader(type1);
        ClassReader classReader2 = getClassReader(type2);

        if (classReader1 == null || classReader2 == null) {
            return OBJECT;
        }

        // 交叉判断
        String interfaceName;
        if (isInterface(classReader1)) {
            interfaceName = type1;
            if (isImplements(interfaceName, classReader2)) {
                return interfaceName;
            }
            if (isInterface(classReader2)) {
                interfaceName = type2;
                if (isImplements(interfaceName, classReader1)) {
                    return interfaceName;
                }
            }
            return OBJECT;
        }

        if (isInterface(classReader2)) {
            interfaceName = type2;
            if (isImplements(interfaceName, classReader1)) {
                return interfaceName;
            }
            return OBJECT;
        }

        // class.
        final Set<String> superClassNames = new HashSet<>();
        superClassNames.add(type1);
        superClassNames.add(type2);

        String superClassName1 = classReader1.getSuperName();
        if (!superClassNames.add(superClassName1)) {
            return superClassName1;
        }

        String superClassName2 = classReader2.getSuperName();
        if (!superClassNames.add(superClassName2)) {
            return superClassName2;
        }

        while (superClassName1 != null || superClassName2 != null) {
            if (superClassName1 != null) {
                superClassName1 = getSuperClassName(superClassName1);
                if (superClassName1 != null) {
                    if (!superClassNames.add(superClassName1)) {
                        return superClassName1;
                    }
                }
            }

            if (superClassName2 != null) {
                superClassName2 = getSuperClassName(superClassName2);
                if (superClassName2 != null) {
                    if (!superClassNames.add(superClassName2)) {
                        return superClassName2;
                    }
                }
            }
        }
        return OBJECT;
    }


    /**
     * 递归判断 class 是否是interfaceName对应的实现类
     *
     * @param interfaceName 接口名称
     * @param classReader   xxx
     * @return true/false
     */
    private boolean isImplements(String interfaceName, ClassReader classReader) {
        ClassReader classInfo = classReader;
        while (classInfo != null) {
            String[] interfaceNames = classInfo.getInterfaces();
            for (String name : interfaceNames) {
                if (name != null && name.equals(interfaceName)) {
                    return true;
                }
            }

            for (String name : interfaceNames) {
                ClassReader interfaceInfo = getClassReader(name);
                if (interfaceInfo != null && isImplements(interfaceName, interfaceInfo)) {
                    return true;
                }
            }

            String superClassName = classInfo.getSuperName();
            if (superClassName == null || superClassName.equals(OBJECT)) {
                break;
            }

            // 读取父类的文件流
            classInfo = getClassReader(superClassName);
        }

        return false;
    }

    private boolean isInterface(final ClassReader classReader) {
        return (classReader.getAccess() & Opcodes.ACC_INTERFACE) != 0;
    }

    private String getSuperClassName(final String className) {
        final ClassReader classReader = getClassReader(className);
        if (classReader == null) {
            return null;
        }
        return classReader.getSuperName();
    }

    private ClassReader getClassReader(final String className) {
        InputStream inputStream = null;
        try {
            inputStream = classLoader.getResourceAsStream(className + ".class");
            return new ClassReader(inputStream);
        } catch (IOException ignored) {

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }


}
