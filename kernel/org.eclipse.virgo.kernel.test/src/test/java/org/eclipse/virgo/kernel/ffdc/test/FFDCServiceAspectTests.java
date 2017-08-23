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

package org.eclipse.virgo.kernel.ffdc.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.eclipse.virgo.kernel.test.AbstractKernelIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;


/**
 */
public class FFDCServiceAspectTests extends AbstractKernelIntegrationTest {

    @Ignore("[DMS-2884] Cannot weave test projects in Ant yet")
    @Test
    //
    public void ffdcAspectTriggersDump() {
        int countBefore = dumpFileCount();
        try {
            new Foo().dodgy();
        } catch (RuntimeException e) {
            // ignore
        }
        int countAfter = dumpFileCount();
        assertEquals(countBefore + 1, countAfter);
    }

    private int dumpFileCount() {
        File dumpDir = new File("build/dumpfiles");
        String[] list = dumpDir.list();
        return (list == null ? 0 : list.length);
    }

    private static class Foo {

        public void dodgy() {
            throw new RuntimeException();
        }
    }
}
