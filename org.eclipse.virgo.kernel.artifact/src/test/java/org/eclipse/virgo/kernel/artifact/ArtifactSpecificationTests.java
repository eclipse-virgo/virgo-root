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

package org.eclipse.virgo.kernel.artifact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;


public class ArtifactSpecificationTests {

    @Test
    public void testCreateWithProperties() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("foo", "bar");
        ArtifactSpecification spec = new ArtifactSpecification("t", "n", new VersionRange("1.2.3"), props);
        assertEquals("bar", spec.getProperties().get("foo"));
    }
    
    @Test
    public void testCreateWithoutProperties() {
        ArtifactSpecification spec = new ArtifactSpecification("t", "n", new VersionRange("1.2.3"));
        assertNotNull(spec.getProperties());
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testPropertiesImmutable() {
        ArtifactSpecification spec = new ArtifactSpecification("t", "n", new VersionRange("1.2.3"));
        spec.getProperties().put("foo", "bar");
    }
}
