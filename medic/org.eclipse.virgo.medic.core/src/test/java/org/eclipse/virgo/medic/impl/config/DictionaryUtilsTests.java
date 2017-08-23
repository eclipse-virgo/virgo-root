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

package org.eclipse.virgo.medic.impl.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.virgo.medic.impl.config.DictionaryUtils;
import org.junit.Test;

public class DictionaryUtilsTests {

    @Test
    public void emptyMerge() {
        Dictionary<Object, Object> primary = new Hashtable<Object, Object>();
        Dictionary<Object, Object> secondary = new Hashtable<Object, Object>();

        DictionaryUtils.merge(primary, secondary);
        assertTrue(primary.isEmpty());
    }

    @Test
    public void mergeWithoutClash() {
        Dictionary<Object, Object> primary = new Hashtable<Object, Object>();
        primary.put("b", "c");
        Dictionary<Object, Object> secondary = new Hashtable<Object, Object>();
        secondary.put("a", "b");

        DictionaryUtils.merge(primary, secondary);
        assertEquals("c", primary.get("b"));
        assertEquals("b", primary.get("a"));
    }

    @Test
    public void mergeWithClash() {
        Dictionary<Object, Object> primary = new Hashtable<Object, Object>();
        primary.put("b", "c");
        Dictionary<Object, Object> secondary = new Hashtable<Object, Object>();
        secondary.put("b", "b");

        DictionaryUtils.merge(primary, secondary);
        assertEquals("c", primary.get("b"));
    }
}
