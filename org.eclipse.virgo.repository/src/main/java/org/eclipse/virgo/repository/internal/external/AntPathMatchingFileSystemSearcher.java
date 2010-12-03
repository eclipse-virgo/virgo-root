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

package org.eclipse.virgo.repository.internal.external;

import java.io.File;
import java.io.IOException;

import org.eclipse.virgo.util.io.FileSystemUtils;

/**
 * A {@link FileSystemSearcher} implementation that searches based on an Ant-style search pattern. See
 * {@link AntPathMatcher} for more details of the syntax.
 * <p />
 * Files that match according to {@link AntPathMatcher#match(String, String)} are considered to be terminal matches.
 * Files that match according to {@link AntPathMatcher#matchStart(String, String)} are considered to be non-terminal
 * matches.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * This class is <strong>thread-safe</strong>.
 * 
 */
public final class AntPathMatchingFileSystemSearcher implements FileSystemSearcher {

    private static final String REGEX_MATCHING_BACKSLASH = "\\\\";

    private static final String BACKSLASH_SEPARATOR = "\\";

    private final File rootDir;

    private final String antPathPattern;

    private final AntPathMatcher antPathMatcher;       

    File getRootDir() {
        return this.rootDir;
    }
    
    boolean matchPath(String filePath) {
        return this.antPathMatcher.match(this.antPathPattern, filePath);
    }
    
    /**
     * Creates a new instance that will locate files based on the supplied path pattern.
     * The supplied path pattern <strong>must</strong> be absolute.
     *
     * @param antPathPattern The Ant path pattern to match against.
     */
    public AntPathMatchingFileSystemSearcher(String antPathPattern) {
        if (!isAbsolute(antPathPattern)) {
            throw new IllegalArgumentException(String.format("The search pattern '%s' is not absolute.", antPathPattern));
        }      
  
        // NB: cannot cope with arbitrary separator character -- assumes syntax for regex argument of split()
        String[] pathComponents = antPathPattern.split(File.separator.equals(BACKSLASH_SEPARATOR) ? REGEX_MATCHING_BACKSLASH : File.separator);
        
        StringBuilder rootPathBuilder = new StringBuilder();
        StringBuilder antPatternBuilder = new StringBuilder();
        boolean rootPathBuilt = false;

        for (String component : pathComponents) {
            if (!rootPathBuilt && !component.contains("*") && !component.contains("?")) {
                rootPathBuilder.append(File.separator);
                rootPathBuilder.append(component);
            } else {
                rootPathBuilt = true;
                antPatternBuilder.append(File.separator);
                antPatternBuilder.append(component);
            }
        }
        try {
            this.rootDir = new File(rootPathBuilder.toString()).getCanonicalFile();
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to determine canonical root directory from '" + rootPathBuilder.toString() + "'", ioe);
        }
        this.antPathMatcher = new AntPathMatcher();
        this.antPathMatcher.setPathSeparator(File.separator);
        this.antPathPattern = this.rootDir.getPath() + antPatternBuilder.toString();       
    }
    
    private static boolean isAbsolute(String searchPattern) {    
        if (!searchPattern.startsWith("/") && !(searchPattern.indexOf(":") > 0)) {
            return false;
        } else {
            return true;
        }        
    }

    /**
     * {@inheritDoc}
     */    
    public void search(SearchCallback callback) {
        search(this.rootDir, callback);
    }

    private void search(File file, SearchCallback callback) {
        String absolutePath = file.getAbsolutePath();
        if (this.antPathMatcher.match(this.antPathPattern, absolutePath)) {
            callback.found(file, true);
        } else if (file.isDirectory() && this.antPathMatcher.matchStart(this.antPathPattern, absolutePath)) {
            callback.found(file, false);
        }
        
        if (file.isDirectory()) {
            for (File fileInDir : FileSystemUtils.listFiles(file)) {
                search(fileInDir, callback);
            }
        }        
    }
    
    @Override
    public String toString() {
    	return this.antPathPattern;
    }
}
