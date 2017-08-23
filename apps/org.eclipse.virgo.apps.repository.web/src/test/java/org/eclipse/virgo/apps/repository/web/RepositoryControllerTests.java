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

package org.eclipse.virgo.apps.repository.web;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.virgo.apps.repository.web.RepositoryController;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.eclipse.virgo.apps.repository.core.RepositoryIndex;
import org.eclipse.virgo.apps.repository.core.RepositoryManager;

public class RepositoryControllerTests {

    private RepositoryManager repositoryManager;

    private RepositoryController repositoryController;

    @Before
    public void setup() {
        repositoryManager = createMock(RepositoryManager.class);
        repositoryController = new RepositoryController(this.repositoryManager);
    }

    @Test
    public void getIndex() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setRequestURI("http://localhost:8080/org.eclipse.virgo.server.repository/my-repo");
        request.setMethod("GET");

        byte[] indexBytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };

        RepositoryIndex repositoryIndex = createMock(RepositoryIndex.class);

        try (InputStream indexBytesAsStream = new ByteArrayInputStream(indexBytes)) {
            expect(repositoryIndex.getInputStream()).andReturn(indexBytesAsStream);
            expect(repositoryIndex.getETag()).andReturn("123456789").anyTimes();
            expect(repositoryIndex.getLength()).andReturn(indexBytes.length);
            
            expect(this.repositoryManager.getIndex("my-repo")).andReturn(repositoryIndex);
            
            replay(this.repositoryManager, repositoryIndex);
            
            repositoryController.getIndex(request, response);
            
            verify(this.repositoryManager, repositoryIndex);
            
            assertEquals("application/org.eclipse.virgo.repository.Index", response.getContentType());
            assertArrayEquals(indexBytes, response.getContentAsByteArray());
        }
    }

    @Test
    public void getIndexForUnknownRepository() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setRequestURI("http://localhost:8080/org.eclipse.virgo.server.repository/my-repo");
        request.setMethod("GET");

        expect(this.repositoryManager.getIndex("my-repo")).andReturn(null);

        replay(this.repositoryManager);

        repositoryController.getIndex(request, response);

        verify(this.repositoryManager);

        assertEquals(response.getStatus(), HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void getArtefact() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setRequestURI("http://localhost:8080/org.eclipse.virgo.server.repository/my-repo/bundle/com.foo/1.0.0");
        request.setMethod("GET");

        byte[] artefactBytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };

        expect(this.repositoryManager.getArtifact("my-repo", "bundle", "com.foo", "1.0.0")).andReturn(new ByteArrayInputStream(artefactBytes));

        replay(this.repositoryManager);

        repositoryController.getArtifact(request, response);

        verify(this.repositoryManager);

        assertEquals("application/octet-stream", response.getContentType());
        assertArrayEquals(artefactBytes, response.getContentAsByteArray());
    }

    @Test
    public void getUnknownArtefact() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setRequestURI("http://localhost:8080/org.eclipse.virgo.server.repository/my-repo/bundle/com.foo/1.0.0");
        request.setMethod("GET");

        expect(this.repositoryManager.getArtifact("my-repo", "bundle", "com.foo", "1.0.0")).andReturn(null);

        replay(this.repositoryManager);

        repositoryController.getArtifact(request, response);

        verify(this.repositoryManager);

        assertEquals(response.getStatus(), HttpServletResponse.SC_NOT_FOUND);
    }
}
