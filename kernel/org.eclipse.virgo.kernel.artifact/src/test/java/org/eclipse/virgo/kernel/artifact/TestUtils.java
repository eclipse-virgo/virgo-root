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

package org.eclipse.virgo.kernel.artifact;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static java.nio.file.Files.walk;
import static org.junit.Assert.fail;

public class TestUtils {

    private static final Path GRADLE_CACHE = Paths.get(System.getProperty("user.home"),
            ".gradle", "caches", "modules-2", "files-2.1");

    public static File fromGradleCache(String bundleName, String key) throws IOException {
        String filename = bundleName + "-" + resolveVersionFromGradleProperties(key) + ".jar";

        return walk(GRADLE_CACHE)
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
