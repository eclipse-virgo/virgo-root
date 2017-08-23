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

package org.eclipse.virgo.repository.internal.management;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.DuplicateArtifactException;
import org.eclipse.virgo.repository.internal.ArtifactDescriptorDepository;
import org.eclipse.virgo.repository.internal.MutableRepository;
import org.eclipse.virgo.repository.internal.management.StandardExternalStorageRepositoryInfo;
import org.eclipse.virgo.repository.management.ArtifactDescriptorSummary;
import org.junit.Test;
import org.osgi.framework.Version;


public class StandardExternalStorageRepositoryInfoTests extends AbstractRepositoryInfoTests {
    
    private static final String LOCATIONS_TEST_FILENAME = "a.jar";

    private final MutableRepository repository = createMock(MutableRepository.class);
    
    
    private final ArtifactDescriptorDepository depository = createMock(ArtifactDescriptorDepository.class);
    
    private final StandardExternalStorageRepositoryInfo repositoryInfo = getRepositoryInfo(this.repository, this.depository);
    
    @Test
    public void publish() throws Exception {                     
        
        String artifactUri = "file:foo.jar";
        ArtifactDescriptor artifactDescriptor = createMock(ArtifactDescriptor.class);
        expect(this.repository.publish(URI.create(artifactUri))).andReturn(artifactDescriptor);        
        
        expect(artifactDescriptor.getType()).andReturn("bundle");
        expect(artifactDescriptor.getName()).andReturn("foo");
        expect(artifactDescriptor.getVersion()).andReturn(new Version(1,0,0));
        
        replay(this.repository, artifactDescriptor, this.depository);
        
        ArtifactDescriptorSummary published = repositoryInfo.publish(artifactUri);
        
        assertEquals("bundle", published.getType());
        assertEquals("foo", published.getName());
        assertEquals("1.0.0", published.getVersion());
        
        verify(this.repository, artifactDescriptor, this.depository);
    }
    
    /**
     * Notice that the expected exception is RuntimeException and not IllegalArgumentException -- this is because the
     * util.jmx aspect 'cleans' the exceptions so as to prevent foreign types leaking across the jmx interface.
     * @throws Exception RuntimeException, in fact
     */
    @Test(expected=RuntimeException.class)
    public void publishDuplicate() throws Exception {                     
        
        String artifactUri = "file:foo.jar";
        ArtifactDescriptor original = createMock(ArtifactDescriptor.class);
        ArtifactDescriptor duplicate = createMock(ArtifactDescriptor.class);
        expect(this.repository.publish(URI.create(artifactUri))).andThrow(new DuplicateArtifactException(original, duplicate));        
        expect(original.getUri()).andReturn(URI.create("file:/original"));
                
        
        replay(this.repository, original, duplicate, this.depository);
               
        try {
            repositoryInfo.publish(artifactUri);
        } finally {
            verify(this.repository, original, duplicate, this.depository);
        }
    }
    
    @Test
    public void retract() {
        
        expect(this.repository.retract("bundle", "foo", new Version(1,0,0))).andReturn(true);
        
        replay(this.repository, this.depository);
        
        assertTrue(repositoryInfo.retract("bundle", "foo", "1.0.0"));
        
        verify(this.repository, this.depository);
    }
    
    @Test
    public void retractMissingArtifact() {
        
        expect(this.repository.retract("bundle", "foo", new Version(1,0,0))).andReturn(false);
        
        replay(this.repository, this.depository);
            
        assertFalse(repositoryInfo.retract("bundle", "foo", "1.0.0"));
        
        verify(this.repository, this.depository);
    }
    
    @Test
    public void getArtifactLocations() throws Exception {
        expect(this.repository.getArtifactLocations(LOCATIONS_TEST_FILENAME)).andReturn(new HashSet<String>());
        replay(this.repository);
        
        Set<String> filepaths = repositoryInfo.getArtifactLocations(LOCATIONS_TEST_FILENAME);
        assertNotNull(filepaths);

        verify(this.repository);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected StandardExternalStorageRepositoryInfo getRepositoryInfo(ArtifactDescriptorDepository depository) {
        return getRepositoryInfo(this.repository, depository);        
    }
    
    private static StandardExternalStorageRepositoryInfo getRepositoryInfo(MutableRepository repository, ArtifactDescriptorDepository depository) {
        StandardExternalStorageRepositoryInfo repositoryInfo = new StandardExternalStorageRepositoryInfo("unittest", depository, repository);              
        return repositoryInfo;
    }
}
