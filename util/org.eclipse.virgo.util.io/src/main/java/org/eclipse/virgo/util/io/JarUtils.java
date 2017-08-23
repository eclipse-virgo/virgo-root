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

package org.eclipse.virgo.util.io;

import java.io.IOException;

/**
 * Utility code for working with JAR files.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Threadsafe
 */
public final class JarUtils {

    /**
     * Unpacks the JAR file at {@link PathReference jarFile} to the directory <code>dest</code>.<p/>
     * 
     * If the supplied <code>dest</code> {@link PathReference} does not exist, it is created as a directory and the
     * JAR file is unpacked <strong>directly</strong> into the newly created directory.<p/>
     * 
     * If the supplied <code>dest</code> <code>PathReference</code> already exists and is a directory then the JAR
     * file is unpacked as a subdirectory of the supplied directory this directory. The name of the generated
     * subdirectory is that of the JAR file without the file extension - so <code>foo.jar</code> is unpacked into a
     * directory <code>foo</code>.
     * 
     * @param jarFile the JAR file to unpack
     * @param dest the destination directory
     * @throws IOException if an error occurs during unpack.
     */
    public static void unpackTo(PathReference jarFile, PathReference dest) throws IOException {
        ZipUtils.unzipTo(jarFile, dest);
    }
    
    /**
     * Unpacks the JAR file at {@link PathReference jarFile} to the directory <code>dest</code>.<p/>
     * 
     * If the supplied <code>dest</code> {@link PathReference} does not exist, it is created as a directory and the
     * JAR file is unpacked <strong>directly</strong> into the newly created directory.<p/>
     * 
     * If the supplied <code>dest</code> <code>PathReference</code> already exists and is a directory then its content
     * is deleted and the JAR file is unpacked in the cleaned directory. 
     * 
     * @param jarFile the JAR file to unpack
     * @param dest the destination directory
     * @throws IOException if an error occurs during unpack.
     */
    public static void unpackToDestructive(PathReference jarFile, PathReference dest) throws IOException {
        ZipUtils.unzipToDestructive(jarFile, dest);
    }
}
