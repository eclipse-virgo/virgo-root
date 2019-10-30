/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.userregion.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static java.nio.file.Files.walk;
import static org.junit.Assert.fail;

public class TestUtils {

    private static final Path BND_PLATFORM = Paths.get("..", "..", "3rd-party", "build", "plugins");

    public static File fromBndPlatform(String locationInBndPlatform) throws IOException {
        Path bundlePath = walk(BND_PLATFORM)
                .filter(s -> s.toString().contains(locationInBndPlatform))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to locate '" + locationInBndPlatform + "' in " + BND_PLATFORM));
        return bundlePath.toFile();
    }

    public static File fromGradleCache(String bundleName, String key) throws IOException {
        String filename = bundleName + "-" + resolveVersionFromGradleProperties(key) + ".jar";
        Path gradleFiles = Paths.get(System.getProperty("user.home"),
                ".gradle", "caches", "modules-2", "files-2.1");

        return walk(gradleFiles)
                .filter(path -> path.endsWith(filename))
                .peek(System.out::println)
                .findFirst().orElseThrow(() -> new IllegalStateException("Failed to find '" + filename + "' in Gradle cache."))
                .toFile();
    }

    private static String resolveVersionFromGradleProperties(String libraryName) {
        String versionString = "unresolved";
        String gradlePropertiesFile = "../../gradle.properties";
        try {
            Properties gradleProperties = new Properties();
            gradleProperties.load(new FileInputStream(gradlePropertiesFile));
            if (!gradleProperties.containsKey(libraryName)) {
                fail("Couldn't resolve '" + libraryName + "' in '" + gradlePropertiesFile + "'.");
            }
            return gradleProperties.getProperty(libraryName);
        } catch (IOException e) {
            fail("Failed to load '" + gradlePropertiesFile + " ' to get version for '" + libraryName + "'.");
        }
        return versionString;
    }
}
