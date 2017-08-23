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

package org.eclipse.virgo.repository.internal.remote;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.ArtifactDescriptorPersister;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.DuplicateArtifactException;
import org.eclipse.virgo.repository.IndexFormatException;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.XmlArtifactDescriptorPersister;
import org.eclipse.virgo.repository.codec.XMLRepositoryCodec;
import org.eclipse.virgo.repository.internal.ArtifactDescriptorDepository;
import org.eclipse.virgo.repository.internal.RepositoryLogEvents;
import org.eclipse.virgo.repository.internal.StandardArtifactDescriptorDepository;

/**
 * An implementation of the {@link ArtifactDescriptorDepository} that allows safe mutation of the delegate instance of
 * {@link ArtifactDescriptorDepository}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
final class MutableArtifactDescriptorDepository implements ArtifactDescriptorDepository {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MutableArtifactDescriptorDepository.class);
	
	private final String repositoryName;
	
	private final EventLogger eventLogger;
    
    private volatile ArtifactDescriptorDepository delegate;
    
    private volatile DescriptorStore descriptorStore = null;
    
    MutableArtifactDescriptorDepository(String repositoryName, EventLogger eventLogger) {
    	this.repositoryName = repositoryName;
    	this.eventLogger = eventLogger;
    }

    /**
     * {@inheritDoc}
     */
    public void addArtifactDescriptor(RepositoryAwareArtifactDescriptor artifactDesc) throws DuplicateArtifactException {
    	throw new UnsupportedOperationException();         
    }

    /**
     * {@inheritDoc}
     */
    public int getArtifactDescriptorCount() {
    	ArtifactDescriptorDepository localDelegate = this.delegate;
    	
    	if (localDelegate != null) {
    		return localDelegate.getArtifactDescriptorCount();
    	} else {
    		return 0;
    	}        
    }

    /**
     * {@inheritDoc}
     */
    public void persist() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public RepositoryAwareArtifactDescriptor removeArtifactDescriptor(URI uri) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean removeArtifactDescriptor(RepositoryAwareArtifactDescriptor artifactDescriptor) {
    	throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public Set<RepositoryAwareArtifactDescriptor> resolveArtifactDescriptors(Set<Attribute> filters) {
    	ArtifactDescriptorDepository localDelegate = this.delegate;
    	
    	if (localDelegate != null) {
        	return localDelegate.resolveArtifactDescriptors(filters);
        } else {
        	return Collections.<RepositoryAwareArtifactDescriptor>emptySet();            
        }
    }
    
    void setDescriptorStore(DescriptorStore descriptorStore) {
    	if (descriptorStore == null) {
    		descriptorStoreUnavailable();
    	} else {
    		descriptorStoreAvailable(descriptorStore);
    	}
    }
    
    private void descriptorStoreUnavailable() {
    	DescriptorStore locaDescriptorStore = this.descriptorStore;
    	
    	if (locaDescriptorStore != null) {
    		deleteDescriptorStore(locaDescriptorStore);
    		this.eventLogger.log(RepositoryLogEvents.REPOSITORY_TEMPORARILY_UNAVAILABLE, this.repositoryName);
    	}
    	
    	this.descriptorStore = null;
    	this.delegate = null;
    }
    
    private void descriptorStoreAvailable(DescriptorStore descriptorStore) {
    	DescriptorStore localDescriptorStore = this.descriptorStore;
    	
    	if (!descriptorStore.equals(localDescriptorStore)) {
    		newDescriptorStoreAvailable(descriptorStore, localDescriptorStore);
    	}
    }
    
    private void newDescriptorStoreAvailable(DescriptorStore newDescriptorStore, DescriptorStore oldDescriptorStore) {    	
		ArtifactDescriptorDepository newDelegate = createNewDepository(newDescriptorStore);
		
		if (newDelegate == null) {
			deleteDescriptorStore(newDescriptorStore);
			deleteDescriptorStore(oldDescriptorStore);
			descriptorStoreUnavailable();
		} else {			
			this.delegate = newDelegate;
			this.descriptorStore = newDescriptorStore;
			
			if (oldDescriptorStore == null) {
    			this.eventLogger.log(RepositoryLogEvents.REPOSITORY_AVAILABLE, this.repositoryName);
        	} else {
        		this.eventLogger.log(RepositoryLogEvents.REPOSITORY_INDEX_UPDATED, this.repositoryName);
        		deleteDescriptorStore(oldDescriptorStore);
        	}
		}	
    }

	private ArtifactDescriptorDepository createNewDepository(DescriptorStore descriptorStore) {
		ArtifactDescriptorPersister persister = new XmlArtifactDescriptorPersister(new XMLRepositoryCodec(), this.repositoryName, descriptorStore.getLocation());
		try {
			 return new StandardArtifactDescriptorDepository(persister);    			    		
		} catch (IndexFormatException ife) {
			LOGGER.error("The descriptor store for repository '{}' was corrupt", this.repositoryName);			
		}
		return null;
	}
		 
    private void deleteDescriptorStore(DescriptorStore descriptorStore) {
    	if (descriptorStore != null && !descriptorStore.getLocation().delete()) {
    		LOGGER.warn("Failed to delete descriptor store for repository '{}' from '{}'", this.repositoryName, descriptorStore.getLocation().getAbsolutePath());
    	}
    }
}
