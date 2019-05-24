package com.ifog.plugin.base;

import com.ifog.plugin.ExtensionClassWriter;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.FileTime;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


/**
 * 处理注入主要逻辑的基类
 *
 * @author: created by hewei
 * @date: 2019/5/17
 * Copyright (c) 2019 https://www.gthanos.cn. All rights reserved.
 */

public class BaseByteInjector implements IByteInjector {

    private ClassLoader classLoader;
    private static final FileTime ZERO = FileTime.fromMillis(0);


    @Override
    public boolean isInjectorClass(String className) {
        return className.endsWith(".class")
                && !className.contains("R$")
                && !className.contains("R.class")
                && !className.contains("BuildConfig.class");

    }

    @Override
    public byte[] processClassToByteArray(InputStream inputStream) {
        ClassReader classReader = null;
        try {
            classReader = new ClassReader(inputStream);
            ClassWriter classWriter = new ExtensionClassWriter(classLoader, ClassWriter.COMPUTE_MAXS);
            ClassVisitor classWriterWrapper = wrapClassWriter(classWriter);
            classReader.accept(classWriterWrapper, ClassReader.EXPAND_FRAMES);
            return classWriter.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void processClassFile(File inputFile, File outputFile, String inputBaseDir) {
        if (!inputBaseDir.endsWith(File.separator)) {
            inputBaseDir = inputBaseDir + File.separator;
        }

        if (isInjectorClass(inputFile.getAbsolutePath().replace(inputBaseDir, "").replace(File.separator, "."))) {
            InputStream inputStream = null;
            FileOutputStream fos = null;
//            System.out.println("===begin=:inputPath" + inputFile.getPath() + ",outputPath:" + outputFile.getPath());

            try {
                FileUtils.touch(outputFile);
                inputStream = new FileInputStream(inputFile);
                byte[] bytes = processClassToByteArray(inputStream);
                fos = new FileOutputStream(outputFile);
                fos.write(bytes);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
//            System.out.println("===end=:inputPath" + inputFile.getPath() + ",outputPath:" + outputFile.getPath());

        } else {
            if (inputFile.isFile()) {
                try {
                    FileUtils.touch(outputFile);
                    FileUtils.copyFile(inputFile, outputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void processJar(File inputJar, File outputJar) throws IOException {
        ZipFile inputZip = new ZipFile(inputJar);
        ZipOutputStream outputZip = new ZipOutputStream(new BufferedOutputStream(
                java.nio.file.Files.newOutputStream(outputJar.toPath())));
        Enumeration<? extends ZipEntry> inEntries = inputZip.entries();
        while (inEntries.hasMoreElements()) {
            ZipEntry entry = inEntries.nextElement();
            InputStream originalFile =
                    new BufferedInputStream(inputZip.getInputStream(entry));
            ZipEntry outEntry = new ZipEntry(entry.getName());
            byte[] newEntryContent;
            if (!isInjectorClass(outEntry.getName().replace(File.separator, "."))) {
                newEntryContent = org.apache.commons.io.IOUtils.toByteArray(originalFile);
            } else {
                newEntryContent = processClassToByteArray(originalFile);
            }
            CRC32 crc32 = new CRC32();
            crc32.update(newEntryContent);
            outEntry.setCrc(crc32.getValue());
            outEntry.setMethod(ZipEntry.STORED);
            outEntry.setSize(newEntryContent.length);
            outEntry.setCompressedSize(newEntryContent.length);
            outEntry.setLastAccessTime(ZERO);
            outEntry.setLastModifiedTime(ZERO);
            outEntry.setCreationTime(ZERO);
            outputZip.putNextEntry(outEntry);
            outputZip.write(newEntryContent);
            outputZip.closeEntry();
        }
        outputZip.flush();
        outputZip.close();
    }


    // 此处实现注入
    public ClassVisitor wrapClassWriter(ClassWriter classWriter) {
        return classWriter;
    }

}
