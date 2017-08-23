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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.eclipse.virgo.util.common.Assert;
import org.eclipse.virgo.util.common.IterableEnumeration;
import org.eclipse.virgo.util.common.StringUtils;



/**
 * Utility code for working with Zip files.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Thread-safe.
 * 
 */
public class ZipUtils {
    
    private static final String ZIP_FILENAME_SUFFIX = ".zip";
    private static final int BUFFER_SIZE = 2048;
    
    /**
     * Unzips the Zip file at {@link PathReference jarFile} to the directory <code>dest</code>.<p/>
     * 
     * If the supplied <code>dest</code> {@link PathReference} does not exist, it is created as a directory and the
     * Zip is unzipped <strong>directly</strong> into the newly created directory.<p/>
     * 
     * If the supplied <code>dest</code> <code>PathReference</code> already exists and is a directory then the Zip
     * is unzipped as a subdirectory of the supplied directory. The name of the generated subdirectory is that of the
     * Zip file without the file extension, e.g. <code>foo.zip</code> is unpacked into a directory <code>foo</code>.
     * 
     * @param zipFile the Zip file to unzip
     * @param dest the destination directory
     * @return a <code>PathReference</code> to the directory containing the Zip file's contents.
     * @throws IOException if an error occurs during unzip.
     */
    
    public static PathReference unzipTo(PathReference zipFile, PathReference dest) throws IOException {
        return unzipToFolder(zipFile, dest, false);
    }
    
    /**
     * Unzips the Zip file at {@link PathReference jarFile} to the directory <code>dest</code>.<p/>
     * 
     * If the supplied <code>dest</code> {@link PathReference} does not exist, it is created as a directory and the
     * Zip is unzipped <strong>directly</strong> into the newly created directory.<p/>
     * 
     * If the supplied <code>dest</code> <code>PathReference</code> already exists and is a directory then its content
     * is deleted and the JAR file is unpacked in the cleaned directory.
     * 
     * @param zipFile the Zip file to unzip
     * @param dest the destination directory
     * @return a <code>PathReference</code> to the directory containing the Zip file's contents.
     * @throws IOException if an error occurs during unzip.
     */
    
    public static PathReference unzipToDestructive(PathReference zipFile, PathReference dest) throws IOException {
        return unzipToFolder(zipFile, dest, true);
    }
    
    /**
     * Zips the file or directory at {@link PathReference toZip} and writes the resulting Zip file to the
     * supplied destination.
     * <p/> 
     * If the supplied destination does not exist the resulting Zip file will be written to that destination. If the supplied
     * destination exists, and it is a directory, the Zip file will be written to that directory with the name as the path
     * to be zipped, suffixed with ".zip". If the supplied destination exists, and it is not a directory, and exception
     * is thrown.
     * <p/>
     * The entries in the resulting zip file are named relative to the path <code>toZip</code>.
     * <p/>
     * @param toZip The file or directory from which the Zip file is to be created.
     * @param dest The path to which the created Zip file is to be written.
     * @throws IOException if an error occurs during the zip process.
     * @return a <code>PathReference</code> to the created Zip file.
     */
    public static PathReference zipTo(PathReference toZip, PathReference dest) throws IOException {
        Assert.isTrue(toZip.exists(), "Supplied file or directory '%s' must exist", toZip);
        
        PathReference finalDest = determineFinalZipDestination(toZip, dest);    
        
        ZipOutputStream zos = null;
        
        try {
            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(finalDest.toFile())));       
            doZip(zos, toZip.toFile(), toZip.toFile(), null);
        } finally {
            if (zos != null) {
                zos.close();
            }
        }
        
        return finalDest;
    }
    
    /**
     * Zips the file or directory at {@link PathReference toZip} and writes the resulting Zip file to the
     * supplied directory, <code>dest</code>.
     * <p/> 
     * If the supplied destination does not exist the resulting Zip file will be written to that destination. If the supplied
     * destination exists, and it is a directory, the Zip file will be written to that directory with the name as the path
     * to be zipped, suffixed with ".zip". If the supplied destination exists, and it is not a directory, and exception
     * is thrown.
     * <p/>
     * Entries in the resulting zip file are named relative to the path <code>toZip</code>. Each entry's name
     * is prefixed with the supplied <code>entryPrefix</code>.
     * <p/>
     * @param toZip The file or directory from which the Zip file is to be created.
     * @param dest The path to which the created Zip file is to be written.
     * @param entryPrefix for all entry names, null means no prefix
     * @return final Zip destination {@link PathReference}
     * @throws IOException if an error occurs during the zip process.
     */
    public static PathReference zipTo(PathReference toZip, PathReference dest, String entryPrefix) throws IOException {
        Assert.isTrue(toZip.exists(), "Supplied file or directory '%s' must exist", toZip);
        
        PathReference finalDest = determineFinalZipDestination(toZip, dest);    
        
        ZipOutputStream zos = null;
        
        try {
            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(finalDest.toFile())));       
            doZip(zos, toZip.toFile(), toZip.toFile(), entryPrefix);
        } finally {
            if (zos != null) {
                zos.close();
            }
        }
        
        return finalDest;
    }
    
    @SuppressWarnings("unchecked")
    private static PathReference unzipToFolder(PathReference zipFile, PathReference dest, boolean isDestructive) throws IOException {
        Assert.isTrue(zipFile.exists(), "Supplied file '%s' must exist", zipFile);
        PathReference finalDest = determineFinalUnzipDestination(zipFile, dest, isDestructive);
        ZipFile zip = null;
        try {
            zip = new ZipFile(zipFile.toFile());
            for (ZipEntry entry : new IterableEnumeration<ZipEntry>((Enumeration<ZipEntry>)zip.entries())) {
                PathReference entryPath = finalDest.newChild(entry.getName());
                if (entry.isDirectory()) {
                    entryPath.createDirectory();
                } else {
                    PathReference filePath = entryPath.createFile();
                    InputStream inputStream = zip.getInputStream(entry);
                    FileCopyUtils.copy(inputStream, new FileOutputStream(filePath.toFile()));
                }
            }
        } finally {
            if (zip != null) {
                zip.close();
            }
        }
        return finalDest;
    }
    
    private static void doZip(ZipOutputStream zos, File file, File root, String entryPrefix) throws IOException {
        
        byte[] data = new byte[BUFFER_SIZE];
        
        String entryName = determineNameOfEntry(file, root, entryPrefix);
        
        if (file.isDirectory()) {
            
            if (!file.equals(root)) {           
                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);
                zos.closeEntry();
            }
            
            for (File dirFile : FileSystemUtils.listFiles(file)) {
                doZip(zos, dirFile, root, entryPrefix);
            }
        } else {
            InputStream is = null;
            try {
                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);
    
                is = new BufferedInputStream(new FileInputStream(file));
                int count;
                while ((count = is.read(data)) > 0) {
                    zos.write(data, 0, count);
                }
                zos.closeEntry();
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }

    private static String determineNameOfEntry(File file, File root, String entryPrefix) {
        String fileName = file.getAbsolutePath().substring(root.getAbsolutePath().length());
        String entryName;
        if (entryPrefix == null) {
            entryName = fileName;
        } else {
            entryName = entryPrefix + fileName;
        }
        
        if (file.isDirectory() && ! entryName.endsWith("/")) {
            entryName = entryName + "/";
        }
        
        return entryName;
    }
    
    private static PathReference determineFinalUnzipDestination(PathReference zipFile, PathReference dest, boolean isDestructive) {
        PathReference finalDest = dest;
        if (!dest.exists()) {
            dest.createDirectory();
        } else {
            if (dest.isDirectory()) {
            	if (isDestructive == true) {
            		boolean isDeleted = dest.delete(true);
            		if (!isDeleted) {
            			throw new FatalIOException("Content of destination path '" + dest + "' cannot be removed");
            		}
            		dest.createDirectory();
            	} else {
            		String destDir = StringUtils.stripFilenameExtension(StringUtils.getFilename(zipFile.getName()));
            		finalDest = dest.newChild(destDir).createDirectory();
            	}
            } else {
                throw new FatalIOException("Destination path '" + dest + "' already exists and is not a directory");
            }
        }
        return finalDest;
    }
    
    private static PathReference determineFinalZipDestination(PathReference toZip, PathReference dest) {
        if (dest.exists()) {
            if (!dest.isDirectory()) {
                throw new FatalIOException("Destination path '" + dest + "' already exists and is not a directory");
            } else {
                return dest.newChild(toZip.getName() + ZIP_FILENAME_SUFFIX);
            }
        } else {
            return dest;
        }
    }
  
}
