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

package org.eclipse.virgo.repository.codec;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.IndexFormatException;
import org.eclipse.virgo.repository.codec.XMLRepositoryCodec;
import org.eclipse.virgo.repository.internal.StandardArtifactDescriptor;
import org.eclipse.virgo.repository.internal.StandardAttribute;
import org.junit.Test;
import org.osgi.framework.Version;


public class XMLRepositoryCodecTests {

    private final XMLRepositoryCodec serializer = new XMLRepositoryCodec();

    @Test
    public void codec() throws FileNotFoundException, IndexFormatException {
        FileOutputStream out = new FileOutputStream("build/repository.xml");
        serializer.write(createArtifacts(), out);
        

        Set<ArtifactDescriptor> artifacts = serializer.read(new FileInputStream("build/repository.xml"));
        assertEquals(createArtifacts(), artifacts);
    }

    private Set<ArtifactDescriptor> createArtifacts() {
        Set<Attribute> attributes = new HashSet<Attribute>();
        attributes.add(new StandardAttribute("attribute1", ""));
        Map<String, Set<String>> properties = new HashMap<String, Set<String>>();
        Set<String> values = new HashSet<String>();
        values.add("value1");
        values.add("value2");
        properties.put("property1", values);
        properties.put("property2", values);
        attributes.add(new StandardAttribute("attribute2", "value2", properties));
        Set<ArtifactDescriptor> artifacts = new HashSet<ArtifactDescriptor>();
        artifacts.add(new StandardArtifactDescriptor(URI.create("http://uri/1"), "bundle", "artifact1", new Version(1, 0, 0), null, attributes));
        artifacts.add(new StandardArtifactDescriptor(URI.create("http://uri/2"), "bundle", "artifact2", new Version(2, 0, 0), "2", attributes));
        return artifacts;
    }
}
