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

package org.eclipse.virgo.shell.internal.completers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.eclipse.virgo.shell.internal.completers.InstallCompleter;
import org.junit.Ignore;
import org.junit.Test;

/**
 */
public class InstallCompleterTests {

    private static final String FILE_PREFIX = "file:";

    private static final File base = new File("build" + File.separatorChar + "install-completer" + File.separatorChar);

    private static final String ALPHA = FILE_PREFIX + new File(base, "alpha ").getPath();

    private static final String ALPHA_SINGLE = FILE_PREFIX + new File(base, "alpha").getPath() + File.separatorChar;

    private static final String APPLE = FILE_PREFIX + new File(base, "apple.txt ").getPath();

    private static final String BRAVO = FILE_PREFIX + new File(base, "bravo ").getPath();

    private static final String BRAVO_SINGLE = FILE_PREFIX + new File(base, "bravo").getPath() + File.separatorChar;

    private final InstallCompleter completer;

    {
        try {
            this.completer = new InstallCompleter();

            new File(base, "alpha").mkdirs();
            new File(base, "bravo").mkdirs();
            new File(base, "apple.txt").createNewFile();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void completionOfCompletelyUnmatchingPath() {
        List<String> completionCandidates = this.completer.getCompletionCandidates("file:zebra" + File.separatorChar + "giraffe");
        assertCompletionsPresent(completionCandidates);
    }
    
    @Ignore("[DMS-2885] FileNameComplet(e|o)r no longer available.")
    @Test
    public void completionWithMultipleOptions() {
        List<String> completionCandidates = this.completer.getCompletionCandidates("file:target" + File.separatorChar + "install-completer"
            + File.separatorChar);
        assertCompletionsPresent(completionCandidates, ALPHA, APPLE, BRAVO);

        completionCandidates = this.completer.getCompletionCandidates("file:target" + File.separatorChar + "install-completer" + File.separatorChar
            + "a");
        assertCompletionsPresent(completionCandidates, ALPHA, APPLE);
    }

    @Ignore("[DMS-2885]FileNameComplet(e|o)r no longer available.")
    @Test
    public void completionWithSingleOption() {
        List<String> completionCandidates = this.completer.getCompletionCandidates("file:target" + File.separatorChar + "install-completer"
            + File.separatorChar + "ap");
        assertEquals(1, completionCandidates.size());
        assertCompletionsPresent(completionCandidates, APPLE);

        completionCandidates = this.completer.getCompletionCandidates("file:target" + File.separatorChar + "install-completer" + File.separatorChar
            + "al");
        assertCompletionsPresent(completionCandidates, ALPHA_SINGLE);

        completionCandidates = this.completer.getCompletionCandidates("file:target" + File.separatorChar + "install-completer" + File.separatorChar
            + "b");
        assertCompletionsPresent(completionCandidates, BRAVO_SINGLE);
    }

    @Test
    public void completionWithNoOptions() {
        List<String> completionCandidates = this.completer.getCompletionCandidates("file:target" + File.separatorChar + "install-completer"
            + File.separatorChar + "c");
        assertCompletionsPresent(completionCandidates);

        completionCandidates = this.completer.getCompletionCandidates("file:target" + File.separatorChar + "install-completer" + File.separatorChar
            + "bravo" + File.separatorChar);
        assertCompletionsPresent(completionCandidates);
    }

    private static void assertCompletionsPresent(List<String> actual, String... expected) {
        assertEquals(expected.length, actual.size());
        for (String string : expected) {
            assertTrue("Expected completion candidate '" + string + "' was not present in completions " + actual, actual.contains(string));
        }
    }
}
