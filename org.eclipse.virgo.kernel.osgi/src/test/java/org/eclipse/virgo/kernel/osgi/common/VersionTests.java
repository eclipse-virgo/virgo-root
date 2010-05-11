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

package org.eclipse.virgo.kernel.osgi.common;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.kernel.osgi.common.Version;
import org.junit.Assert;
import org.junit.Test;

public class VersionTests {

    @Test public void normal() {
        Version v1 = new Version("1");
        Version v10 = new Version("1.0");
        Assert.assertEquals(v1, v10);
        Version v101 = new Version("1.0.1");
        Assert.assertEquals(0, v1.compareTo(v10));
        Assert.assertEquals(-1, v1.compareTo(v101));
        Assert.assertEquals(1, v101.compareTo(v10));
    }

    @Test public void edgeCases() {

        Assert.assertEquals(0, (new Version("0")).compareTo(new Version("")));
        Assert.assertEquals(0, (new Version("0")).compareTo(new Version((String) null)));

        try {
            @SuppressWarnings("unused") Version v2 = new Version(".");
            Assert.assertTrue(false);
        } catch (NumberFormatException e) {
        }
        try {
            @SuppressWarnings("unused") Version v3 = new Version("0-0");
            Assert.assertTrue(false);
        } catch (NumberFormatException e) {
        }

        try {
            @SuppressWarnings("unused") Version v4 = new Version("1.-3.5");
            Assert.assertTrue(false);
        } catch (IllegalArgumentException e) {
        }
    }

    @Test public void listConstructor() {
        Version v102 = new Version("1.0.2");
        List<Integer> comps = new ArrayList<Integer>();
        comps.add(1);
        comps.add(0);
        comps.add(2);
        Version v102a = new Version(comps);
        Assert.assertEquals(0, v102.compareTo(v102a));
    }

}
