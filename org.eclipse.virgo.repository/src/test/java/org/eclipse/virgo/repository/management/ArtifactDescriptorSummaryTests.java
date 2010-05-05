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

package org.eclipse.virgo.repository.management;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.virgo.repository.management.ArtifactDescriptorSummary;
import org.junit.Test;


/**
 * Tests for {@link ArtifactDescriptorSummary}
 * 
 */
public class ArtifactDescriptorSummaryTests {

    private final ArtifactDescriptorSummary adsNull = new ArtifactDescriptorSummary(null,null,null);
    private final ArtifactDescriptorSummary adsStd = new ArtifactDescriptorSummary("type","name","version");
    private final ArtifactDescriptorSummary adsStd2 = new ArtifactDescriptorSummary("type","name","version");
    private final ArtifactDescriptorSummary adsStd3 = new ArtifactDescriptorSummary("type","name1","version");
    
    @Test
    public void nullCase() {
        assertEquals(null, adsNull.getName());
        assertEquals(null, adsNull.getType());
        assertEquals(null, adsNull.getVersion());
    }
    @Test
    public void nonNullCase() {
        assertEquals("name", adsStd.getName());
        assertEquals("type", adsStd.getType());
        assertEquals("version", adsStd.getVersion());
    }
    
    @Test
    public void stringOf() {
        assertEquals("type: type, name: name, version: version", adsStd.toString());
        assertEquals("type: null, name: null, version: null", adsNull.toString());
    }
    
    @Test
    public void testHash() {
        int h = adsNull.hashCode();
        assertTrue(h==adsNull.hashCode());
        assertFalse(h==adsStd.hashCode());
        assertTrue(adsStd.hashCode()==adsStd2.hashCode());
        assertFalse(adsStd.hashCode()==adsStd3.hashCode());
    }  
    
    @Test
    public void testEquals() {
        assertTrue(adsStd.equals(adsStd2));
        assertFalse(adsStd.equals(adsStd3));
    }
}
