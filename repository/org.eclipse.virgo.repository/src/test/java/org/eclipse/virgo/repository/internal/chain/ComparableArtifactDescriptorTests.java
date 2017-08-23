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

package org.eclipse.virgo.repository.internal.chain;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.internal.chain.ChainedQuery;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;


public class ComparableArtifactDescriptorTests {

    private RepositoryAwareArtifactDescriptor artefact;

    private RepositoryAwareArtifactDescriptor comparableArtefact;

    @Before
    public void setup() {
        this.artefact = createMock(RepositoryAwareArtifactDescriptor.class);
        this.comparableArtefact = new ChainedQuery.ComparableArtifactDescriptor(0, artefact);
    }

    @Test
    public void getAttributesByName() {
        Set<Attribute> attributeSet = new HashSet<Attribute>();
        String name = "name";
        expect(this.artefact.getAttribute("name")).andReturn(attributeSet);
        replay(this.artefact);
        assertEquals(attributeSet, this.comparableArtefact.getAttribute(name));
        verify(this.artefact);
    }

    @Test
    public void getAttributes() {
        Set<Attribute> attributeSet = new HashSet<Attribute>();
        expect(this.artefact.getAttributes()).andReturn(attributeSet);
        replay(this.artefact);
        assertEquals(attributeSet, this.comparableArtefact.getAttributes());
        verify(this.artefact);
    }

    @Test
    public void getType() {
        String type = "type";
        expect(this.artefact.getType()).andReturn(type);
        replay(this.artefact);
        assertEquals(type, this.comparableArtefact.getType());
        verify(this.artefact);
    }

    @Test
    public void getName() {
        String name = "name";
        expect(this.artefact.getName()).andReturn(name);
        replay(this.artefact);
        assertEquals(name, this.comparableArtefact.getName());
        verify(this.artefact);
    }

    @Test
    public void getUri() {
        URI uri = URI.create("artefact://foo");
        expect(this.artefact.getUri()).andReturn(uri);
        replay(this.artefact);
        assertEquals(uri, this.comparableArtefact.getUri());
        verify(this.artefact);
    }

    @Test
    public void getVersion() {
        Version version = new Version("1.2.3");
        expect(this.artefact.getVersion()).andReturn(version);
        replay(this.artefact);
        assertEquals(version, this.comparableArtefact.getVersion());
        verify(this.artefact);
    }

}
