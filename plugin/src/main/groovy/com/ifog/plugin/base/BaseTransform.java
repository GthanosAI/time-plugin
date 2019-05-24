package com.ifog.plugin.base;

import com.android.build.api.transform.Context;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.ide.common.internal.WaitableExecutor;
import com.google.common.io.Files;
import com.ifog.plugin.ClassLoaderHelper;
import com.ifog.plugin.RunVariant;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author: created by hewei
 * @date: 2019/5/16
 * Copyright (c) 2019 https://www.gthanos.cn. All rights reserved.
 */

public abstract class BaseTransform extends Transform {
    private static final Set<QualifiedContent.Scope> SCOPES = new HashSet<>();
    private Logger logger;
    protected Project project;
    private WaitableExecutor waitableExecutor;
    private BaseByteInjector injector;
    private boolean cleanDexBuilderFolder = false;
    private boolean skip = false;


    static {
        SCOPES.add(QualifiedContent.Scope.PROJECT);
        SCOPES.add(QualifiedContent.Scope.SUB_PROJECTS);
        SCOPES.add(QualifiedContent.Scope.EXTERNAL_LIBRARIES);
    }

    public BaseTransform(Project project) {
        this.project = project;
        this.logger = project.getLogger();
        this.waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool();
        injector = getBaseInjector();
    }

    protected abstract BaseByteInjector getBaseInjector();


    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return SCOPES;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }


    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        long spendTime = System.currentTimeMillis();
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        boolean isIncremental = transformInvocation.isIncremental();
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();

        if (!isIncremental) {
            // 非增量编译是清除所有文件
            outputProvider.deleteAll();
        }

        initSkip(transformInvocation.getContext());


        setClassLoader(inputs, transformInvocation);
        handler(inputs, transformInvocation.getOutputProvider(), isIncremental);
        waitableExecutor.waitForTasksWithQuickFail(true);
        spendTime = System.currentTimeMillis() - spendTime;
        logger.warn(getName() + ": cost time" + spendTime + "ms");

    }

    private void setClassLoader(Collection<TransformInput> inputs, TransformInvocation transformInvocation) throws MalformedURLException {
        URLClassLoader urlClassLoader = ClassLoaderHelper.getClassLoader(inputs, transformInvocation.getReferencedInputs(), project);
        this.injector.setClassLoader(urlClassLoader);
    }


    private void handler(Collection<TransformInput> inputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException {

        for (TransformInput input : inputs) {
            // jar input
            for (JarInput jarInput : input.getJarInputs()) {
                Status status = jarInput.getStatus();
                File dest = outputProvider.getContentLocation(
                        jarInput.getFile().getAbsolutePath(),
                        jarInput.getContentTypes(),
                        jarInput.getScopes(),
                        Format.JAR
                );
                handlerJar(jarInput, isIncremental, status, dest);
            }

            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                File dest = outputProvider.getContentLocation(
                        directoryInput.getFile().getAbsolutePath(),
                        directoryInput.getContentTypes(),
                        directoryInput.getScopes(),
                        Format.DIRECTORY);

                if (isIncremental && !skip) {
                    FileUtils.forceMkdir(dest);
                    String srcDirPath = directoryInput.getFile().getAbsolutePath();
                    String destDirPath = dest.getAbsolutePath();
                    Map<File, Status> fileStatusMap = directoryInput.getChangedFiles();
                    for (Map.Entry<File, Status> changeFile : fileStatusMap.entrySet()) {
                        Status status = changeFile.getValue();
                        File inputFile = changeFile.getKey();
                        String destFilePath = inputFile.getAbsolutePath().replace(srcDirPath, destDirPath);
                        File destFile = new File(destFilePath);
                        handlerDir(status, inputFile, destFile, srcDirPath);
                    }
                } else {
                    tranformDir(directoryInput.getFile(), dest);
                }
            }
        }
    }


    private void handlerJar(JarInput jarInput, boolean isIncremental, Status status, File dest) throws IOException {
        if (isIncremental && !skip) {
            switch (status) {
                case NOTCHANGED:
                    break;
                case ADDED:
                case CHANGED:
                    transformJar(jarInput.getFile(), dest, status);
                    break;
                case REMOVED:
                    if (dest.exists()) {
                        FileUtils.forceDelete(dest);
                    }
                    break;
            }
        } else {
            //Forgive me!, Some project will store 3rd-party aar for serveral copies in dexbuilder folder,,unknown issue.
            if (!isIncremental && !cleanDexBuilderFolder) {
                cleanDexBuilderFolder(dest);
                cleanDexBuilderFolder = true;
            }
            transformJar(jarInput.getFile(), dest, status);
        }
    }

    private void handlerDir(Status status, File inputFile, File destFile, String srcDirPath) throws IOException {
        switch (status) {
            case NOTCHANGED:
                break;
            case REMOVED:
                if (destFile.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    destFile.delete();
                }
                break;
            case ADDED:
            case CHANGED:
                try {
                    FileUtils.touch(destFile);
                } catch (IOException e) {
                    //maybe mkdirs fail for some strange reason, try again.
                    Files.createParentDirs(destFile);
                }
                transformSingleFile(inputFile, destFile, srcDirPath);
                break;
        }
    }

    private void transformSingleFile(File inputFile, File outFile, String srcBaseDir) {
        waitableExecutor.execute(() -> {
            injector.processClassFile(inputFile, outFile, srcBaseDir);
            return null;
        });
    }

    /**
     * 处理jarFile
     *
     * @param srcJar
     * @param destJar
     * @param status
     */
    private void transformJar(File srcJar, File destJar, Status status) {
        waitableExecutor.execute(() -> {
            if (skip) {
                FileUtils.copyFile(srcJar, destJar);
                return null;
            }
            injector.processJar(srcJar, destJar);
            return null;
        });
    }

    private void tranformDir(File inputDir, File outputDir) throws IOException {
        if (skip) {
            FileUtils.copyDirectory(inputDir, outputDir);
            return;
        }

        final String inputDirPath = inputDir.getAbsolutePath();
        final String outputDirPath = outputDir.getAbsolutePath();
        if (inputDir.isDirectory()) {
            for (final File file : com.android.utils.FileUtils.getAllFiles(inputDir)) {
                waitableExecutor.execute(() -> {
                    String filePath = file.getAbsolutePath();
                    File outputFile = new File(filePath.replace(inputDirPath, outputDirPath));
                    injector.processClassFile(file, outputFile, inputDirPath);
                    return null;
                });
            }
        }

    }

    private void cleanDexBuilderFolder(File dest) {
        waitableExecutor.execute(() -> {
            try {
                String dexBuilderDir = replaceLastPart(dest.getAbsolutePath(), getName(), "dexBuilder");
                //intermediates/transforms/dexBuilder/debug
                File file = new File(dexBuilderDir).getParentFile();
                project.getLogger().warn("clean dexBuilder folder = " + file.getAbsolutePath());
                if (file.exists() && file.isDirectory()) {
                    com.android.utils.FileUtils.deleteDirectoryContents(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    private String replaceLastPart(String originString, String replacement, String toreplace) {
        int start = originString.lastIndexOf(replacement);
        StringBuilder builder = new StringBuilder();
        builder.append(originString, 0, start);
        builder.append(toreplace);
        builder.append(originString.substring(start + replacement.length()));
        return builder.toString();
    }

    private void initSkip(Context context) {
        RunVariant variant = getRunVariant();
        String variantName = context.getVariantName();

        System.out.println("==============variant:" + getRunVariant() + ", local:" + variantName);
        if (variant == RunVariant.ALL) {
            skip = false;
        } else if (variant == RunVariant.NONE) {
            skip = true;
        } else if (variant == RunVariant.ANNOTATION) {
            skip = false;
        } else {
            if ("debug".equals(variantName) && (variant == RunVariant.DEBUG)) {
                skip = false;
            } else if ("release".equals(variantName) && (variant == RunVariant.RELEASE)) {
                skip = false;
            } else {
                skip = true;
            }
        }
    }


    protected RunVariant getRunVariant() {
        return RunVariant.DEBUG;
    }
}
