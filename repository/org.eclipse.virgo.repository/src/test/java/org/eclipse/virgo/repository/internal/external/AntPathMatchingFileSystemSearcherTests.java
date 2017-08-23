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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.virgo.repository.internal.external.AntPathMatchingFileSystemSearcher;
import org.eclipse.virgo.repository.internal.external.FileSystemSearcher;
import org.eclipse.virgo.repository.internal.external.FileSystemSearcher.SearchCallback;
import org.junit.Test;


/**
 */
public class AntPathMatchingFileSystemSearcherTests {

    @Test
    public void consistentlyDeepDirectoryStructure() throws Exception {
        
        String antPathPattern = new File("src/test/resources/file-system-searcher").getAbsolutePath() + File.separator + "*" + File.separator + "*" + File.separator + "*.txt";
        
        FileSystemSearcher searcher = new AntPathMatchingFileSystemSearcher(antPathPattern);
        ResultTrackingSearchCallback callback = new ResultTrackingSearchCallback();
        searcher.search(callback);
        File root = new File("src/test/resources/file-system-searcher");
        List<File> expectedTerminalMatches = Arrays.asList(new File[] { new File(root, "a/1/a1.txt").getCanonicalFile(),
            new File(root, "b/1/b1.txt").getCanonicalFile(), new File(root, "c/1/c1.txt").getCanonicalFile() });
        assertFound(expectedTerminalMatches, callback.getTerminalMatches());
        List<File> expectedNonTerminalMatches = Arrays.asList(new File[] { root.getCanonicalFile(), new File(root, "a").getCanonicalFile(),
            new File(root, "a/1").getCanonicalFile(), new File(root, "b").getCanonicalFile(), new File(root, "b/1").getCanonicalFile(),
            new File(root, "c").getCanonicalFile(), new File(root, "c/1").getCanonicalFile() });
        assertFound(expectedNonTerminalMatches, callback.getNonTerminalMatches());
    }

    @Test
    public void arbitrarilyDeepDirectoryStructure() throws Exception {
        FileSystemSearcher searcher = new AntPathMatchingFileSystemSearcher(new File("src/test/resources/file-system-searcher").getAbsolutePath() + File.separator + "**" + File.separator + "*");
        ResultTrackingSearchCallback callback = new ResultTrackingSearchCallback();
        searcher.search(callback);
        File root = new File("src/test/resources/file-system-searcher");
        List<File> expectedTerminalMatches = Arrays.asList(new File[] {
            new File(root, "a").getCanonicalFile(),
            new File(root, "a/1").getCanonicalFile(),
            new File(root, "a/1/a1.txt").getCanonicalFile(),
            new File(root, "a/1/z").getCanonicalFile(),
            new File(root, "a/1/z/a1z.txt").getCanonicalFile(),
            new File(root, "a/1/z/file.wont.be.found.in.suffix.search").getCanonicalFile(),
            new File(root, "b").getCanonicalFile(),
            new File(root, "b/1").getCanonicalFile(),
            new File(root, "b/1/b1.txt").getCanonicalFile(),
            new File(root, "b/1/file.wont.be.found.in.suffix.search").getCanonicalFile(),
            new File(root, "c").getCanonicalFile(),
            new File(root, "c/c.txt").getCanonicalFile(),
            new File(root, "c/1").getCanonicalFile(),
            new File(root, "c/1/c1.txt").getCanonicalFile()});                                     
        assertFound(expectedTerminalMatches, callback.getTerminalMatches());
        List<File> expectedNonTerminalMatches = Arrays.asList(new File[] {new File("src/test/resources/file-system-searcher").getCanonicalFile()});
        assertFound(expectedNonTerminalMatches, callback.getNonTerminalMatches());
    }
    
    @Test
    public void trailingStarStarDirectoryStructure() throws Exception {
        FileSystemSearcher searcher = new AntPathMatchingFileSystemSearcher(new File("src/test/resources/file-system-searcher").getAbsolutePath() + File.separator + "**");
        ResultTrackingSearchCallback callback = new ResultTrackingSearchCallback();
        searcher.search(callback);
        File root = new File("src/test/resources/file-system-searcher");
        List<File> expectedTerminalMatches = Arrays.asList(new File[] {
            new File("src/test/resources/file-system-searcher").getCanonicalFile(),
            new File(root, "a").getCanonicalFile(),
            new File(root, "a/1").getCanonicalFile(),
            new File(root, "a/1/a1.txt").getCanonicalFile(),
            new File(root, "a/1/z").getCanonicalFile(),
            new File(root, "a/1/z/a1z.txt").getCanonicalFile(),
            new File(root, "a/1/z/file.wont.be.found.in.suffix.search").getCanonicalFile(),
            new File(root, "b").getCanonicalFile(),
            new File(root, "b/1").getCanonicalFile(),
            new File(root, "b/1/b1.txt").getCanonicalFile(),
            new File(root, "b/1/file.wont.be.found.in.suffix.search").getCanonicalFile(),
            new File(root, "c").getCanonicalFile(),
            new File(root, "c/c.txt").getCanonicalFile(),
            new File(root, "c/1").getCanonicalFile(),
            new File(root, "c/1/c1.txt").getCanonicalFile()});                                     
        assertFound(expectedTerminalMatches, callback.getTerminalMatches());
        List<File> expectedNonTerminalMatches = Arrays.asList(new File[] {});
        assertFound(expectedNonTerminalMatches, callback.getNonTerminalMatches());
    }
    
    @Test
    public void arbitrarilyDeepDirectoryStructureWithSuffix() throws Exception {
        FileSystemSearcher searcher = new AntPathMatchingFileSystemSearcher(new File("src/test/resources/file-system-searcher").getAbsolutePath() + File.separator + "**" + File.separator + "*.txt");
        ResultTrackingSearchCallback callback = new ResultTrackingSearchCallback();
        searcher.search(callback);
        File root = new File("src/test/resources/file-system-searcher");
        List<File> expectedTerminalMatches = Arrays.asList(new File[] {
            new File(root, "a/1/a1.txt").getCanonicalFile(),
            new File(root, "a/1/z/a1z.txt").getCanonicalFile(), 
            new File(root, "b/1/b1.txt").getCanonicalFile(),
            new File(root, "c/c.txt").getCanonicalFile(),
            new File(root, "c/1/c1.txt").getCanonicalFile()});
        assertFound(expectedTerminalMatches, callback.getTerminalMatches());
        List<File> expectedNonTerminalMatches = Arrays.asList(new File[] {
            root.getCanonicalFile(),
            new File(root, "a").getCanonicalFile(),
            new File(root, "a/1").getCanonicalFile(),
            new File(root, "a/1/z").getCanonicalFile(),
            new File(root, "b").getCanonicalFile(),
            new File(root, "b/1").getCanonicalFile(),
            new File(root, "c").getCanonicalFile(),
            new File(root, "c/1").getCanonicalFile() });
        assertFound(expectedNonTerminalMatches, callback.getNonTerminalMatches());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void nonAbsoluteSearchPath() {
        new AntPathMatchingFileSystemSearcher(new File("relative").getPath() + "*/*.foo");
    }

    private void assertFound(List<File> expectedFiles, List<File> actualFiles) {
        List<File> missingFiles = new ArrayList<File>();
        missingFiles.addAll(expectedFiles);
        for (File actual : actualFiles) {
            missingFiles.remove(actual);
        }
        assertTrue(missingFiles + " were missing from the files that were found: " + actualFiles, missingFiles.isEmpty());

        if (actualFiles.size() != expectedFiles.size()) {
            for (File expected : expectedFiles) {
                actualFiles.remove(expected);
            }
            fail("Unexpected files were found: " + actualFiles);
        }
    }

    private static class ResultTrackingSearchCallback implements SearchCallback {

        private final List<File> terminalMatches = new ArrayList<File>();

        private final List<File> nonTerminalMatches = new ArrayList<File>();

        /**
         * {@inheritDoc}
         */
        public void found(File file, boolean terminal) {
            if (!file.getPath().contains(".svn")) {
                if (terminal) {
                    terminalMatches.add(file);
                } else {
                    nonTerminalMatches.add(file);
                }
            }
        }

        private List<File> getTerminalMatches() {
            return this.terminalMatches;
        }

        private List<File> getNonTerminalMatches() {
            return this.nonTerminalMatches;
        }
    }
}
