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
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;

import org.eclipse.virgo.util.common.Assert;
import org.slf4j.Logger;


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
        /* designed to prevent user instantiation of this class */
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
     * @return <code>true</code> if the <code>File</code> was deleted or didn't exist in the first place, otherwise <code>false</code>.
     * @see #deleteRecursively(String)
     */
    public static boolean deleteRecursively(File root) {
        return doRecursiveDelete(root);
    }

    /**
     * Delete the file referenced by the supplied path and, if the path refers to a directory, recursively delete any
     * nested directories or files.
     * 
     * @param path the path to the file or directory to delete.
     * @return <code>true</code> if the <code>File</code> or directory was deleted or didn't exist in the first place, otherwise <code>false</code>.
     * @see #deleteRecursively(File)
     */
    public static boolean deleteRecursively(String path) {
        return deleteRecursively(new File(path));
    }

    /**
     * Generate array of {@link String}s of the names of the files in the directory <code>dir</code> 
     * (just like {@link File#list()}).
     * This function, however, <i>never</i> returns the <strong><code>null</code></strong> pointer, but instead throws
     * an exception if it cannot determine the files, or if <code>dir</code> isn't a directory.
     *  
     * @param dir directory file for which the filenames should be generated.
     * @return array of {@link String}s; may be the empty array.
     * @throws FatalIOException when {@link File#list()} returns <strong><code>null</code></strong> even after a retry.
     */
    public static String[] list(File dir) throws FatalIOException {
        return list(dir, (Logger) null); 
    }
     
    /**
     * Generate array of {@link String}s of the names of the files in the directory <code>dir</code> (just like
     * {@link File#list()}). This function, however, <i>never</i> returns the <strong><code>null</code></strong>
     * pointer, but instead throws an exception if it cannot determine the files, or if <code>dir</code> isn't a
     * directory.
     * 
     * @param dir directory file for which the filenames should be generated.
     * @param logger where to log warnings or errors, if not null
     * @return array of {@link String}s; may be the empty array.
     * @throws FatalIOException when {@link File#list()} returns <strong><code>null</code></strong> even after a retry.
     */
    public static String[] list(File dir, Logger logger) throws FatalIOException {
        String[] filenames = dir.list();
        if (filenames==null) {
            if (logger!=null) logger.warn("'" + dir + "'.list() returned null first time.");
            preRetryFileOp(dir, logger);
            filenames = dir.list();
        }
        if (filenames==null) {
            if (logger!=null) logger.error("'" + dir + "'.list() returned null on retry.");
            throw new FatalIOException("list() failed for file " + dir);
        }
        return filenames;
    }

    /**
     * Generate array of {@link String}s of the names of the files in the directory <code>dir</code>, filtered by <code>filenameFilter</code> 
     * (just like {@link File#list(FilenameFilter)}).
     * This function, however, <i>never</i> returns the <strong><code>null</code></strong> pointer, but instead throws
     * an exception if it cannot determine the files, or if <code>dir</code> isn't a directory.
     *  
     * @param dir directory file for which the filenames should be generated.
     * @param filenameFilter filter on the files' filenames
     * @return array of {@link String}s; may be the empty array.
     * @throws FatalIOException when {@link File#list(FilenameFilter)} returns <strong><code>null</code></strong> even after a retry.
     */
    public static String[] list(File dir, FilenameFilter filenameFilter) throws FatalIOException {
        return list(dir, filenameFilter, null);
    }
    
    /**
     * Generate array of {@link String}s of the names of the files in the directory <code>dir</code>, filtered by <code>filenameFilter</code> 
     * (just like {@link File#list(FilenameFilter)}).
     * This function, however, <i>never</i> returns the <strong><code>null</code></strong> pointer, but instead throws
     * an exception if it cannot determine the files, or if <code>dir</code> isn't a directory.
     *  
     * @param dir directory file for which the filenames should be generated.
     * @param filenameFilter filter on the files' filenames
     * @param logger where to log warnings or errors, if not null
     * @return array of {@link String}s; may be the empty array.
     * @throws FatalIOException when {@link File#list(FilenameFilter)} returns <strong><code>null</code></strong> even after a retry.
     */
    public static String[] list(File dir, FilenameFilter filenameFilter, Logger logger) throws FatalIOException {
        String[] filenames = dir.list(filenameFilter);
        if (filenames==null) {
            if (logger!=null) logger.warn("'" + dir + "'.list(<FilenameFilter>) returned null first time.");
            preRetryFileOp(dir, logger);
            filenames = dir.list(filenameFilter);
        }
        if (filenames==null) {
            if (logger!=null) logger.error("'" + dir + "'.list(<FilenameFilter>) returned null on retry.");
            throw new FatalIOException("list(FilenameFilter) failed for file " + dir);
        }
        return filenames;
    }
    
    /**
     * Generate array of {@link File}s, one for each file in the directory <code>dir</code> (just like {@link File#listFiles()}).
     * This function, however, <i>never</i> returns the <strong><code>null</code></strong> pointer, but instead throws
     * an exception if it cannot determine the files, or if <code>dir</code> isn't a directory.
     *  
     * @param dir directory file for which the files should be generated.
     * @param logger where to log warnings or errors, if not null
     * @return array of {@link File}s; may be the empty array.
     * @throws FatalIOException when {@link File#listFiles()} returns <strong><code>null</code></strong> even after a retry.
     */
    public static File[] listFiles(File dir, Logger logger) throws FatalIOException {
        File[] files = dir.listFiles();
        if (files==null) {
            if (logger!=null) logger.warn("'" + dir + "'.listFiles() returned null first time.");
            preRetryFileOp(dir, logger);
            files = dir.listFiles();
        }
        if (files==null) {
            if (logger!=null) logger.error("'" + dir + "'.listFiles() returned null on retry.");
            throw new FatalIOException("listFiles() failed for file " + dir);
        }
        return files;
    }
    
    /**
     * Generate array of {@link File}s, one for each file in the directory <code>dir</code>, filtered by <code>fileFilter</code> 
     * (just like {@link File#listFiles(FileFilter)}).
     * This function, however, <i>never</i> returns the <strong><code>null</code></strong> pointer, but instead throws
     * an exception if it cannot determine the files, or if <code>dir</code> isn't a directory.
     *  
     * @param dir directory file for which the files should be generated.
     * @param fileFilter filter on the files
     * @return array of {@link File}s; may be the empty array.
     * @throws FatalIOException when {@link File#listFiles(FileFilter)} returns <strong><code>null</code></strong> even after a retry.
     */
    public static File[] listFiles(File dir, FileFilter fileFilter) throws FatalIOException { 
        return listFiles(dir, fileFilter, null);
    }

    /**
     * Generate array of {@link File}s, one for each file in the directory <code>dir</code>, unfiltered 
     * (just like {@link File#listFiles()}).
     * This function, however, <i>never</i> returns the <strong><code>null</code></strong> pointer, but instead throws
     * an exception if it cannot determine the files, or if <code>dir</code> isn't a directory.
     *  
     * @param dir directory file for which the files should be generated.
     * @return array of {@link File}s; may be the empty array.
     * @throws FatalIOException when {@link File#listFiles(FileFilter)} returns <strong><code>null</code></strong> even after a retry.
     */
    public static File[] listFiles(File dir) throws FatalIOException { 
        return listFiles(dir, (Logger) null);
    }

    /**
     * Generate array of {@link File}s, one for each file in the directory <code>dir</code>, filtered by
     * <code>fileFilter</code> (just like {@link File#listFiles(FileFilter)}). This function, however, <i>never</i>
     * returns the <strong><code>null</code></strong> pointer, but instead throws an exception if it cannot determine
     * the files, or if <code>dir</code> isn't a directory.
     * 
     * @param dir directory file for which the files should be generated.
     * @param fileFilter filter on the files
     * @param logger where to log warnings or errors, if not null
     * @return array of {@link File}s; may be the empty array.
     * @throws FatalIOException when {@link File#listFiles(FileFilter)} returns <strong><code>null</code></strong> even
     *         after a retry.
     */
    public static File[] listFiles(File dir, FileFilter fileFilter, Logger logger) throws FatalIOException {
        File[] files = dir.listFiles(fileFilter);
        if (files==null) {
            if (logger!=null) logger.warn("'" + dir + "'.listFiles(<FileFilter>) returned null first time.");
            preRetryFileOp(dir, logger);
            files = dir.listFiles(fileFilter);
        }
        if (files==null) {
            if (logger!=null) logger.error("'" + dir + "'.listFiles(<FileFilter>) returned null on retry.");
            throw new FatalIOException("listFiles(FileFilter) failed for file " + dir);
        }
        return files;
    }
 
    /**
     * Generate array of {@link File}s, one for each file in the directory <code>dir</code>, filtered by <code>filenameFilter</code> 
     * (just like {@link File#listFiles(FilenameFilter)}).
     * This function, however, <i>never</i> returns the <strong><code>null</code></strong> pointer, but instead throws
     * an exception if it cannot determine the files, or if <code>dir</code> isn't a directory.
     *  
     * @param dir directory file for which the files should be generated.
     * @param filenameFilter filter on the files' filenames
     * @return array of {@link File}s; may be the empty array.
     * @throws FatalIOException when {@link File#listFiles(FilenameFilter)} returns <strong><code>null</code></strong> even after a retry.
     */
    public static File[] listFiles(File dir, FilenameFilter filenameFilter) throws FatalIOException {
        return listFiles(dir, filenameFilter, null);
    }

    /**
     * Generate array of {@link File}s, one for each file in the directory <code>dir</code>, filtered by <code>filenameFilter</code> 
     * (just like {@link File#listFiles(FilenameFilter)}).
     * This function, however, <i>never</i> returns the <strong><code>null</code></strong> pointer, but instead throws
     * an exception if it cannot determine the files, or if <code>dir</code> isn't a directory.
     *  
     * @param dir directory file for which the files should be generated.
     * @param filenameFilter filter on the files' filenames
     * @param logger where to log warnings or errors, if not null
     * @return array of {@link File}s; may be the empty array.
     * @throws FatalIOException when {@link File#listFiles(FilenameFilter)} returns <strong><code>null</code></strong> even after a retry.
     */
    public static File[] listFiles(File dir, FilenameFilter filenameFilter, Logger logger) throws FatalIOException {
        File[] files = dir.listFiles(filenameFilter);
        if (files==null) {
            if (logger!=null) logger.warn("'" + dir + "'.listFiles(<FilenameFilter>) returned null first time.");
            preRetryFileOp(dir, logger);
            files = dir.listFiles(filenameFilter);
        }
        if (files==null) {
            if (logger!=null) logger.error("'" + dir + "'.listFiles(<FilenameFilter>) returned null on retry.");
            throw new FatalIOException("listFiles(FilenameFilter) failed for file " + dir);
        }
        return files;
    }

    /**
     * This method is designed to allow the underlying file system time to 're-group' before trying the operation again.
     * <br/>It invokes an operation that will potentially involve some I/O so that transient problems with file access
     * (proto-typically directory lists) have a chance to pass before we retry them.
     * 
     * <p/>Callers typically perform:
     * <pre>
     * result = <strong>file.someOp()</strong>;
     * if (possible transient error) 
     * { <strong>preRetryFileOp(file)</strong>;
     *   result = <strong>file.someOp()</strong>;
     * }
     * if (still possible transient error) 
     *   throw RunTimeException(...);
     * </pre>
     * @param file associated with which transient error may have occurred
     */
    private final static void preRetryFileOp(File file, Logger logger) {
        try {
            file.getCanonicalPath(); // potentially involves I/O
        } catch (IOException ioe) {
            if (logger!=null) logger.warn("PreRetry logic '" + file + "'.getCanonicalPath() threw IOException.", ioe);
        }
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
        return true;
    }
}
