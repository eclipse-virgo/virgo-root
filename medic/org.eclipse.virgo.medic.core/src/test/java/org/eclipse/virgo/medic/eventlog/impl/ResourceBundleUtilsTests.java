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

package org.eclipse.virgo.medic.eventlog.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Locale;

import org.eclipse.virgo.medic.eventlog.impl.ResourceBundleUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ResourceBundleUtilsTests {

    private Locale defaultLocale;

    @Before
    public void setDefaultLocale() {
        this.defaultLocale = Locale.getDefault();
        Locale.setDefault(new Locale("d", "E", "f"));
    }

    @After
    public void restoreDefaultLocale() {
        Locale.setDefault(this.defaultLocale);
    }

    @Test
    public void localeWithVariant() {
        Locale locale = new Locale("l", "C", "v");

        List<String> candidates = ResourceBundleUtils.generateCandidatePropertiesFileNames("foo", locale);

        assertNotNull(candidates);
        assertEquals("Expected 7 candidates but got " + candidates, 7, candidates.size());

        assertEquals("foo_l_C_v.properties", candidates.get(0));
        assertEquals("foo_l_C.properties", candidates.get(1));
        assertEquals("foo_l.properties", candidates.get(2));
        assertEquals("foo_d_E_f.properties", candidates.get(3));
        assertEquals("foo_d_E.properties", candidates.get(4));
        assertEquals("foo_d.properties", candidates.get(5));
        assertEquals("foo.properties", candidates.get(6));
    }

    @Test
    public void localeWithCountry() {
        Locale locale = new Locale("l", "C");

        List<String> candidates = ResourceBundleUtils.generateCandidatePropertiesFileNames("foo", locale);

        assertNotNull(candidates);
        assertEquals("Expected 6 candidates but got " + candidates, 6, candidates.size());

        assertEquals("foo_l_C.properties", candidates.get(0));
        assertEquals("foo_l.properties", candidates.get(1));
        assertEquals("foo_d_E_f.properties", candidates.get(2));
        assertEquals("foo_d_E.properties", candidates.get(3));
        assertEquals("foo_d.properties", candidates.get(4));
        assertEquals("foo.properties", candidates.get(5));
    }

    @Test
    public void localeWithLanguage() {
        Locale locale = new Locale("l");

        List<String> candidates = ResourceBundleUtils.generateCandidatePropertiesFileNames("foo", locale);

        assertNotNull(candidates);
        assertEquals("Expected 5 candidates but got " + candidates, 5, candidates.size());

        assertEquals("foo_l.properties", candidates.get(0));
        assertEquals("foo_d_E_f.properties", candidates.get(1));
        assertEquals("foo_d_E.properties", candidates.get(2));
        assertEquals("foo_d.properties", candidates.get(3));
        assertEquals("foo.properties", candidates.get(4));
    }

    @Test
    public void emptyLocale() {
        Locale locale = new Locale("");

        List<String> candidates = ResourceBundleUtils.generateCandidatePropertiesFileNames("foo", locale);

        assertNotNull(candidates);
        assertEquals("Expected 4 candidates but got " + candidates, 4, candidates.size());

        assertEquals("foo_d_E_f.properties", candidates.get(0));
        assertEquals("foo_d_E.properties", candidates.get(1));
        assertEquals("foo_d.properties", candidates.get(2));
        assertEquals("foo.properties", candidates.get(3));
    }
}
