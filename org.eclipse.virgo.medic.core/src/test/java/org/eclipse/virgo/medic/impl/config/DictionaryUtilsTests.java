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


@SuppressWarnings("unchecked")
public class DictionaryUtilsTests {

    @Test
    public void emptyMerge() {
        Dictionary primary = new Hashtable();
        Dictionary secondary = new Hashtable();

        DictionaryUtils.merge(primary, secondary);
        assertTrue(primary.isEmpty());
    }

    @Test
    public void mergeWithoutClash() {
        Dictionary primary = new Hashtable();
        primary.put("b", "c");
        Dictionary secondary = new Hashtable();
        secondary.put("a", "b");

        DictionaryUtils.merge(primary, secondary);
        assertEquals("c", primary.get("b"));
        assertEquals("b", primary.get("a"));
    }

    @Test
    public void mergeWithClash() {
        Dictionary primary = new Hashtable();
        primary.put("b", "c");
        Dictionary secondary = new Hashtable();
        secondary.put("b", "b");

        DictionaryUtils.merge(primary, secondary);
        assertEquals("c", primary.get("b"));
    }
}
