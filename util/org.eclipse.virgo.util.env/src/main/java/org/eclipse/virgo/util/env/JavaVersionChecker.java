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

package org.eclipse.virgo.util.env;

/**
 * JavaVersionChecker is specifically designed to check for the version of Java being used for Virgo.
 * It is run as part of the startup protocol before the server is launched.
 * <p />
 * If the Java version is correct this call is silent, and returns zero <code>System.exit(0)</code>.<br/>
 * If the version is not set or incorrect, a single line is output (in English) and this process returns -1 <code>System.exit(-1)</code>
 * <p/>
 *
 * <strong>Concurrent Semantics</strong><br />
 * Main thread run synchronously.
 *
 * @see "http://www.oracle.com/technetwork/java/javase/versioning-naming-139433.html"
 */
public class JavaVersionChecker {

    /**
     * No parameters are expected; this version check is specific for Virgo.
     * @param args non expected
     */
    public static void main(String[] args) {
        String javaVersion = System.getProperty("java.version");
        if (javaVersion==null) {
            System.out.println("JavaVersionChecker: no java.version property found.");
            System.exit(-1);
        } 

        if (!javaVersion.startsWith("1.6.") && !javaVersion.startsWith("1.7.") && !javaVersion.startsWith("1.8.")) {
            System.out.println("JavaVersionChecker: Java version must be at 1.6+ (detected version "+javaVersion+").");
            System.exit(-1);
        }
        System.exit(0);
    }
}
