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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;

/**
 * Represents a reference, by path, to a location on the file system. This location may be a file or a directory, or it
 * may point to a non-existent location.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Threadsafe.
 */
public final class PathReference {

    /**
     * The {@link File} handle that backs this reference.
     */
    private final File file;

    /**
     * Creates a new <code>PathReference</code> for the supplied path.
     * 
     * @param path the path to reference.
     */
    public PathReference(String path) {
        this(new File(path));
    }

    /**
     * Creates a new <code>PathReference</code> for the supplied {@link URI} which must point to a file location.
     * 
     * @param uri the file URI to reference
     */
    public PathReference(URI uri) {
        this.file = new File(uri);
    }

    /**
     * Creates a new <code>PathReference</code> for the supplied {@link File}.
     * 
     * @param file the file to reference.
     */
    public PathReference(File file) {
        this.file = file;
    }

    /**
     * Converts this <code>PathReference</code> to a {@link File}.
     * 
     * @return the <code>File</code>.
     */
    public File toFile() {
        return this.file;
    }

    /**
     * Queries whether or not this <code>PathReference</code> points to a vaild file or directory.
     * 
     * @return <code>true</code> if the location is valid, otherwise <code>false</code>.
     */
    public boolean exists() {
        return this.file.exists();
    }

    /**
     * Queries whether or not this <code>PathReference</code> points to a file.
     * 
     * @return <code>true</code> if this location points to a file, otherwise <code>false</code>.
     */
    public boolean isFile() {
        return this.file.isFile();
    }

    /**
     * Queries whether or not this <code>PathReference</code> points to a directory.
     * 
     * @return <code>true</code> if this location points to a directory, otherwise <code>false</code>.
     */
    public boolean isDirectory() {
        return this.file.isDirectory();
    }

    /**
     * Gets the name of the file or directory referenced by this <code>PathReference</code>.
     * 
     * @return the file or directory name.
     * @see File#getName()
     */
    public String getName() {
        return this.file.getName();
    }

    /**
     * Gets the parent directory of this <code>PathReference</code>.
     * 
     * @return the parent directory.
     * @see File#getParentFile()
     */
    public PathReference getParent() {
        File parentFile = this.file.getParentFile();
        return parentFile == null ? null : new PathReference(parentFile);
    }

    /**
     * Queries whether or not the path of this <code>PathReference</code> is absolute.
     * 
     * @return <code>true</code> if the path is absolute, otherwise <code>false</code>.
     */
    public boolean isAbsolute() {
        return this.file.isAbsolute();
    }

    /**
     * Gets the absolute path of this <code>PathReference</code>.
     * 
     * @return the absolute path.
     */
    public String getAbsolutePath() {
        return this.file.getAbsolutePath();
    }

    /**
     * Converts this <code>PathReference</code> into a {@link URI}.
     * 
     * @return the URI of this <code>PathReference</code>.
     * @see File#toURI()
     */
    public URI toURI() {
        return this.file.toURI();
    }

    /**
     * Gets the canonical path of this <code>PathReference</code>.
     * 
     * @return the canonical path.
     */
    public String getCanonicalPath() {
        try {
            return this.file.getCanonicalPath();
        } catch (IOException e) {
            throw new FatalIOException("Unable to canonicalize path for file '" + this + "'.", e);
        }
    }

    /**
     * Creates a <code>PathReference</code> that points to the absolute location of this <code>PathReference</code>.
     * 
     * @return the new <code>PathReference</code>.
     */
    public PathReference toAbsoluteReference() {
        return new PathReference(getAbsolutePath());
    }

    /**
     * Creates a <code>PathReference</code> that points to the canonical location of this <code>PathReference</code>.
     * 
     * @return the new <code>PathReference</code>.
     */
    public PathReference toCanonicalReference() {
        return new PathReference(getCanonicalPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.file.toString();
    }

    /**
     * Delete the file/directory referenced by this <code>PathReference</code>. Has the same effect as calling
     * {@link #delete(boolean) delete(false)}.
     * 
     * @return <code>true</code> if the file is deleted, otherwise <code>false</code>.
     */
    public boolean delete() {
        return delete(false);
    }

    /**
     * Delete the file/directory referenced by this <code>PathReference</code>. Nested contents can optionally be
     * deleted by passing <code>recursive = true</code>. Non-recursive deletes will fail (return <code>false</code> for
     * non-empty directories).
     * 
     * @param recursive should the delete process nested contents recursively.
     * @return <code>true</code> if the file is deleted, otherwise <code>false</code>.
     */
    public boolean delete(boolean recursive) {
        boolean deleted = recursive ? FileSystemUtils.deleteRecursively(this.file) : this.file.delete();
        return deleted;
    }

    /**
     * Same as calling {@link #copy(PathReference, boolean) copy(to, false)}.
     * 
     * @param dest the destination to copy to.
     * @return the final destination <code>PathReference</code>.
     */
    public PathReference copy(PathReference dest) {
        return copy(dest, false);
    }

    /**
     * Same as calling {@link #copy(PathReference, boolean, PathFilter) copy(to, recursive, null)};
     * 
     * @param dest the destination to copy to.
     * @param recursive whether the copy should be recursive or not.
     * @return the final destination <code>PathReference</code>.
     */
    public PathReference copy(PathReference dest, boolean recursive) {
        return copy(dest, recursive, null);
    }

    /**
     * Copies the contents of the file/directory to location referenced supplied <code>PathReference</code>. The exact
     * semantics of the copy depend on the state of both the source and destination locations.
     * <p/>
     * 
     * When copying a file, if the destination location already exists and is a file then a {@link FatalIOException} is
     * thrown. If the destination location exists and is a directory, the file is copied into this directory with the
     * same {@link #getName name} as the source location. If the destination location does not exist, then the file is
     * copied to this location directly.
     * <p/>
     * 
     * When copying a directory, if the destination location exists then the directory is copied into the already
     * existing directory with the same name as the source location. If the destination location does not exist, then
     * the directory is copied directly to the destination location and thus gets the name defined by the destination
     * location.
     * <p/>
     * 
     * A {@link PathFilter} can be used to specify exactly which files will be included in the copy. Passing
     * <code>null</code> for the filter includes all files.
     * <p/>
     * 
     * Non-recursive copies on non-empty directories will fail with {@link FatalIOException}.
     * 
     * @param dest the destination to copy to.
     * @param recursive whether the copy should be recursive or not.
     * @param filter a <code>PathFilter</code> controlling which files are included in the copy.
     * @return the final destination <code>PathReference</code>.
     */
    public PathReference copy(PathReference dest, boolean recursive, PathFilter filter) {
        if (!exists()) {
            throw new FatalIOException("Cannot copy path '" + this + "' to '" + dest + "'. Source path does not exist.");
        }
        if (isFile()) {
            if (dest.exists()) {
                if (dest.isFile()) {
                    throw new FatalIOException("Cannot copy path '" + this + "' to '" + dest + "'. Destination path already exists.");
                } else {
                    dest = dest.newChild(this.file.getName());
                    if (dest.exists()) {
                        throw new FatalIOException("Cannot copy path '" + this + "' to '" + dest + "'. Destination path already exists.");
                    } else {
                        dest.createFile();
                    }
                }
            }
            copyFile(this.file, dest.file);
        } else { // this.file.isDirectory() assumed true
            int length = FileSystemUtils.list(this.file).length;
            if (length > 0 && !recursive) {
                throw new FatalIOException("Cannot perform non-recursive copy on non-empty directory '" + this + "'");
            }
            if (dest.exists()) {
                dest = dest.newChild(this.file.getName()).createDirectory();
            } else {
                // copy to this dir
                dest.createDirectory();
            }
            recursiveCopy(this.file, dest.file, filter);
        }
        return dest;
    }

    /**
     * Move the file (or directory) associated with this path reference to the destination path reference.
     * <p/>
     * If the destination already exists, or if the file associated with this path reference <i>doesn't</i> exist, then
     * a {@link FatalIOException} (unchecked) is thrown. <br/>
     * This is otherwise functionally equivalent to the sequence
     * <code>copy(dest,<strong>true</strong>); delete(<strong>true</strong>);</code> returning the destination
     * reference, except that a <i>fast move</i> is attempted first.
     * <p/>
     * If the <i>fast move</i> fails (for example because of file system limitations) the slow version is attempted,
     * <i>viz</i>:
     * <code>copy(dest,<strong>true</strong>); delete(<strong>true</strong>); <strong>return</strong> dest;</code>
     * 
     * @param dest path to move the current file to
     * @return the destination path reference
     */
    public PathReference moveTo(PathReference dest) {
        if (!exists()) {
            throw new FatalIOException("Cannot move path '" + this + "' to '" + dest + "'. Source file does not exist.");
        }
        if (dest.exists()) {
            throw new FatalIOException("Cannot move path '" + this + "' to '" + dest + "'. Destination path already exists.");
        }
        if (!this.file.renameTo(dest.file)) {
            copy(dest, true); // can throw FatalIOException
            delete(true); // can throw FatalIOException
        }
        return dest;
    }

    /**
     * Move the file (or directory) associated with this path reference to the destination path reference.
     * <p/>
     * If the destination already exists, or if the file associated with this path reference <i>doesn't</i> exist, then
     * a {@link FatalIOException} (unchecked) is thrown. <br/>
     * A <i>fast move</i> is attempted.
     * <p/>
     * If the <i>fast move</i> fails a {@link FataIOException} is thrown.
     * <p/>
     * This is a utility function for testing purposes, and is deliberately package private.
     * 
     * @param dest path to move the current file to
     * @return the destination path reference
     */
    PathReference fastMoveTo(PathReference dest) {
        if (!exists()) {
            throw new FatalIOException("Cannot move path '" + this + "' to '" + dest + "'. Source file does not exist.");
        }
        if (dest.exists()) {
            throw new FatalIOException("Cannot move path '" + this + "' to '" + dest + "'. Destination path already exists.");
        }
        if (!this.file.renameTo(dest.file)) {
            throw new FatalIOException("Fast move from '" + this + "' to '" + dest + "' failed.");
        }
        return dest;
    }

    /**
     * Creates a new <code>PathReference</code> by concatenating the path of this <code>PathReference</code> with the
     * supplied <code>name</code>. Note that this method does <strong>not</strong> create a physical file or directory
     * at the child location.
     * 
     * @param name the name of the new child entry.
     * @return the child <code>PathReference</code>.
     */
    public PathReference newChild(String name) {
        return PathReference.concat(this.file.getPath(), name);
    }

    /**
     * Creates a new file at the location of this <code>PathReference</code>.
     * 
     * @return this <code>PathReference</code> for chaining purposes.
     */
    public PathReference createFile() {
        if (this.exists()) {
            return this;
        }
        File parent = this.file.getParentFile();
        if (!parent.exists()) {
            boolean success = parent.mkdirs();
            if (!success) {
                throw new FatalIOException("Unable to create needed directory " + parent);
            }
        }
        try {
            this.file.createNewFile();
            return this;
        } catch (IOException e) {
            throw new FatalIOException("Unable to create file '" + this + "'.", e);
        }
    }

    /**
     * Creates a directory at the location of this <code>PathReference</code>.
     * 
     * @return this <code>PathReference</code> for chaining purposes.
     */
    public PathReference createDirectory() {
        if (!this.file.exists()) {
            if (!this.file.mkdirs()) {
                throw new FatalIOException("Unable to create directory " + this.file);
            }
        }
        return this;
    }

    /**
     * Creates a new <code>PathReference</code> by concatenating the supplied <code>parts</code>, treating each part as
     * a separate path element.
     * 
     * @param parts the path elements.
     * @return the <code>PathReference</code> for the concatenated path.
     */
    public static PathReference concat(String... parts) {
        StringBuilder sb = new StringBuilder(256);
        for (int x = 0; x < parts.length; x++) {
            String part = parts[x];
            sb.append(part);
            if (x < parts.length && !part.endsWith(File.separator)) {
                sb.append(File.separatorChar);
            }
        }
        return new PathReference(sb.toString());
    }

    /**
     * Recursively copy the contents of <code>src</code> to <code>dest</code>.
     * 
     * @param src the source directory.
     * @param dest the destination file.
     */
    private static void recursiveCopy(File src, File dest, PathFilter filter) {
        for (File file : FileSystemUtils.listFiles(src)) {
            PathReference newFile = PathReference.concat(dest.getAbsolutePath(), file.getName());
            if (filter != null && !filter.matches(new PathReference(file))) {
                continue;
            }
            if (file.isFile()) {
                newFile.createFile();
                copyFile(file, newFile.toFile());
            } else {
                newFile.createDirectory();
                recursiveCopy(file, newFile.toFile(), filter);
            }
        }
    }

    /**
     * Copies <code>src</code> to <code>dest</code>.
     * 
     * @param src the source <code>File</code>.
     * @param dest the destination <code>File</code>.
     */
    private static void copyFile(File src, File dest) {
        try {
            FileCopyUtils.copy(src, dest);
        } catch (IOException e) {
            throw new FatalIOException("Cannot copy " + (src.isFile() ? "file" : "directory") + " '" + src + "' to '" + dest + "'.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.file.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof PathReference)) {
            return false;
        }
        PathReference thatRef = (PathReference) that;
        return this.file.equals(thatRef.file);
    }

    /**
     * Gets the contents of the {@link File} pointed to by this <code>PathReference</code>.
     * 
     * @return the file contents as a <code>String</code>.
     * @throws IOException if the file cannot be found or read.
     */
    public String fileContents() throws IOException {
        if (!this.file.isFile()) {
            throw new FileNotFoundException("PathReference refers to a directory which has no file contents");
        }
        StringBuffer contents = new StringBuffer(100);
        try (Reader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(this.file), UTF_8))) {
            char[] chars = new char[100];
            int charsRead = 0;
            while ((charsRead = fileReader.read(chars)) != -1) {
                contents.append(chars, 0, charsRead);
            }
        }
        return contents.toString();
    }

    /**
     * Set the last modified time of the {@link File} pointed to by this <code>PathReference</code> to the current time.
     * 
     * Note: the behaviour is not the same as UNIX(TM) touch as the latter also sets the file access time.
     * 
     * @return true iff this operation succeeded, false otherwise
     */
    public boolean touch() {
        return this.file.setLastModified(System.currentTimeMillis());
    }

    /**
     * Allows filtering during {@link PathReference} operations.
     * <p/>
     * 
     * <strong>Concurrent Semantics</strong><br />
     * 
     * Implementations needn't be threadsafe if they are not shared by multiple threads.
     * 
     */
    public static interface PathFilter {

        /**
         * Match against the supplied {@link PathReference} to query whether it is included in the outer operation.
         * 
         * @param path the path to match against.
         * @return <code>true</code> if the <code>PathReference</code> should be included, otherwise <code>false</code>.
         */
        boolean matches(PathReference path);
    }
}
