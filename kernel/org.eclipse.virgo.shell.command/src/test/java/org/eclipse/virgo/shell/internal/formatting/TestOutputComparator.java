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

package org.eclipse.virgo.shell.internal.formatting;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.virgo.util.io.IOUtils;

public final class TestOutputComparator {

    public static void assertOutputEquals(File expectedFile, List<String> experimental) {

        BufferedReader in = null;
        Iterator<String> it = experimental.iterator();

        try {
            in = new BufferedReader(new FileReader(expectedFile));
            String line;
            while ((line = in.readLine()) != null) {
                assertEquals(line, it.next());
            }
        } catch (FileNotFoundException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
