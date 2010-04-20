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

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.virgo.util.common.Assert;


/**
 * Utility methods for dealing with the file system.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 */
public final class FileSystemUtils {

    private static final String URI_FILE_SCHEME = "file";

    private FileSystemUtils() {
        /* no-op */
    }

    /**
     * Alias for {@link #convertToCanonicalPath(String, boolean) convertToCanonicalPath(path, false) }.
     * @param path the path to convert; may be <code>null</code>
     * @return the canonical path or <code>null</code> if the supplied path was <code>null</code>
     * @throws FatalIOException if a lower-level {@link IOException} is thrown.
     * 
     * @see #convertToCanonicalPath(String, boolean)
     */
    public static String convertToCanonicalPath(String path) throws FatalIOException {
        return convertToCanonicalPath(path, false);
    }

    /**
     * Converts the supplied <code>path</code> to a {@link File#getCanonicalPath() canonical path}, wrapping any
     * {@link IOException IOExceptions} in a {@link FatalIOException}.
     * 
     * @param path the path to convert; may be <code>null</code>
     * @param verifyPathIsDirectory <code>true</code> if the path should only refer to a directory
     * @return the canonical path or <code>null</code> if the supplied path was <code>null</code>
     * @throws FatalIOException if <code>verifyPathIsDirectory</code> is <code>true</code> and the supplied path is not a directory, or a lower-level {@link IOException} is thrown.
     */
    public static String convertToCanonicalPath(String path, boolean verifyPathIsDirectory) throws FatalIOException {
        if (path == null) {
            return null;
        }
        try {
            File dir = new File(path);
            if (verifyPathIsDirectory && !dir.isDirectory()) {
                throw new FatalIOException("The supplied path [" + path + "] is not a directory.");
            }
            return dir.getCanonicalPath();
        } catch (IOException e) {
            throw new FatalIOException("Could not determine the canonical path for [" + path + "]", e);
        }
    }

    /**
     * Converts the supplied {@link URI} to a {@link File#getCanonicalPath() canonical path}, wrapping any
     * {@link IOException IOExceptions} in a {@link FatalIOException}.
     * 
     * @param uri the path to convert; may be <code>null</code>
     * @return the canonical path or <code>null</code> if the supplied <code>URI</code> was <code>null</code>
     * @throws FatalIOException if a lower-level {@link IOException} is thrown.
     */
    public static String convertToCanonicalPath(URI uri) throws FatalIOException {
        if (uri == null) {
            return null;
        }
        Assert.isTrue(uri.getScheme().equals(URI_FILE_SCHEME), "Cannot determine path of URI '%s' with non-file scheme", uri);
        try {
            return new File(uri).getCanonicalPath();
        } catch (IOException ioe) {
            throw new FatalIOException("Could not determine the canonical path for URI [" + uri + "]", ioe);
        }
    }

    /**
     * Verifies that the directory with the supplied <code>path</code> exists, and if it does not exist, an attempt
     * will be made to create it as well as any necessary parent directories. As a convenience, the supplied path will
     * be {@link #convertToCanonicalPath(String) converted} to a canonical path and returned.
     * 
     * @param path the directory path; must not be <code>null</code>
     * @return the canonical path to the directory
     */
    public static String createDirectoryIfNecessary(String path) {
        Assert.notNull(path, "'path' must not be null.");
        File dir = new File(path);
        if (!dir.exists()) {
            boolean success = dir.mkdirs();
            if(!success) {
            	throw new FatalIOException("Unable to create needed directory " + path);
            }
        }
        return convertToCanonicalPath(path);
    }

    /**
     * Delete the supplied {@link File} and, for directories, recursively delete any nested directories or files.
     * 
     * @param root the root <code>File</code> to delete.
     * @return <code>true</code> if the <code>File</code> was deleted, otherwise <code>false</code>.
     * @see #deleteRecursively(String)
     */
    public static boolean deleteRecursively(File root) {
        boolean success = doRecursiveDelete(root);
        return success;
    }

    /**
     * Delete the file referenced by the supplied path and, if the path refers to a directory, recursively delete any
     * nested directories or files.
     * 
     * @param path the path to the file or directory to delete.
     * @return <code>true</code> if the file or directory was deleted, otherwise <code>false</code>.
     * @see #deleteRecursively(File)
     */
    public static boolean deleteRecursively(String path) {
        return deleteRecursively(new File(path));
    }

    private static boolean doRecursiveDelete(File root) {
        if (root.exists()) {
            if (root.isDirectory()) {
                File[] children = root.listFiles();
				if(children != null) {
					for (File file : children) {
	                    doRecursiveDelete(file);
	                }
				}
            }
            return root.delete();
        }
        return false;
    }
}
