/*
 * Copyright Perforator, Inc. and contributors. All rights reserved.
 *
 * Use of this software is governed by the Business Source License
 * included in the LICENSE file.
 *
 * As of the Change Date specified in that file, in accordance with
 * the Business Source License, use of this software will be governed
 * by the Apache License, Version 2.0.
 */
package io.perforator.sdk.loadgenerator.codeless.actions;

import com.google.common.reflect.ClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceLoaderDescriptorGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLoaderDescriptorGenerator.class);

    public static void main(String[] args) throws Exception {
        if (args == null || args.length > 1 || args.length == 0) {
            throw new RuntimeException(
                    "Argument with the target directory is required"
            );
        }

        new ServiceLoaderDescriptorGenerator().generateServicesDescriptor(
                Path.of(args[0])
        );
    }

    public void generateServicesDescriptor(Path targetDirectory) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        ClassPath classPath = ClassPath.from(classLoader);
        Class actionProcessorClass = loadClass(classLoader, ActionProcessor.class.getName());

        List<String> classNames = classPath.getTopLevelClassesRecursive(
                actionProcessorClass.getPackageName()
        ).stream().filter(
                c -> isActionProcessorImplementation(
                        classLoader,
                        c,
                        actionProcessorClass
                )
        ).map(
                c -> c.getName()
        ).collect(
                Collectors.toList()
        );

        if (classNames.isEmpty()) {
            throw new RuntimeException(
                    "No implementations of "
                            + actionProcessorClass
                            + " found"
            );
        }

        if (!Files.exists(targetDirectory)) {
            Files.createDirectories(targetDirectory);
        }

        Path descriptorPath = targetDirectory.resolve(
                actionProcessorClass.getName()
        );

        Files.write(descriptorPath, classNames);

        LOGGER.info(
                "Services descriptor for {} has been successfully generated and saved to {} using the following implementations: {}",
                actionProcessorClass.getName(),
                descriptorPath.toAbsolutePath(),
                classNames
        );
    }

    private boolean isActionProcessorImplementation(ClassLoader classLoader, ClassPath.ClassInfo classInfo, Class actionProcessorClass) {
        Class candidateClass = loadClass(classLoader, classInfo.getName());
        int modifiers = candidateClass.getModifiers();

        return Modifier.isPublic(modifiers)
                && !Modifier.isInterface(modifiers)
                && !Modifier.isAbstract(modifiers)
                && actionProcessorClass.isAssignableFrom(candidateClass);
    }

    private Class loadClass(ClassLoader classLoader, String className) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "Can't load class " + className,
                    e
            );
        }
    }

}
