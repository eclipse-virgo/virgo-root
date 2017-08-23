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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.junit.Test;

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

    @Test(expected = UnsupportedOperationException.class)
    public void testPropertiesImmutable() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("foo", "bar");
        ArtifactSpecification spec = new ArtifactSpecification("t", "n", new VersionRange("1.2.3"), props);
        spec.getProperties().put("a", "b");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEmptyPropertiesImmutable() {
        ArtifactSpecification spec = new ArtifactSpecification("t", "n", new VersionRange("1.2.3"));
        spec.getProperties().put("foo", "bar");
    }

    @Test
    public void testCreateWithUrlAndProperties() throws Exception {
        Map<String, String> props = new HashMap<String, String>();
        props.put("foo", "bar");
        URI uri = new URI("file:x.y");
        ArtifactSpecification spec = new ArtifactSpecification(uri, props);
        assertEquals(uri, spec.getUri());
    }

    @Test
    public void testCreateWithUrlWithoutProperties() throws Exception {
        URI uri = new URI("file:x.y");
        ArtifactSpecification spec = new ArtifactSpecification(uri);
        assertEquals(uri, spec.getUri());
    }

}
