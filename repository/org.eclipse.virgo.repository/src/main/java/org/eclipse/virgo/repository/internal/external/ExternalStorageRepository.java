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

package org.eclipse.virgo.repository.internal.external;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.management.JMException;

import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.ArtifactDescriptorPersister;
import org.eclipse.virgo.repository.DuplicateArtifactException;
import org.eclipse.virgo.repository.IndexFormatException;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.RepositoryCreationException;
import org.eclipse.virgo.repository.XmlArtifactDescriptorPersister;
import org.eclipse.virgo.repository.codec.XMLRepositoryCodec;
import org.eclipse.virgo.repository.configuration.ExternalStorageRepositoryConfiguration;
import org.eclipse.virgo.repository.internal.ArtifactDescriptorDepository;
import org.eclipse.virgo.repository.internal.MutableRepository;
import org.eclipse.virgo.repository.internal.PersistentRepository;
import org.eclipse.virgo.repository.internal.RepositoryLogEvents;
import org.eclipse.virgo.repository.internal.management.StandardExternalStorageRepositoryInfo;
import org.eclipse.virgo.repository.management.RepositoryInfo;
import org.eclipse.virgo.util.io.FileSystemUtils;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe
 * 
 */
public final class ExternalStorageRepository extends PersistentRepository implements MutableRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalStorageRepository.class);

    private final EventLogger eventLogger;

    private final AntPathMatchingFileSystemSearcher antPathMatchingFileSystemSearcher;

    public ExternalStorageRepository(ExternalStorageRepositoryConfiguration configuration, EventLogger eventLogger)
        throws RepositoryCreationException, IndexFormatException {
        this(configuration, new XmlArtifactDescriptorPersister(new XMLRepositoryCodec(), configuration.getName(), configuration.getIndexLocation()), eventLogger);
    }
    
    public ExternalStorageRepository(ExternalStorageRepositoryConfiguration configuration, ArtifactDescriptorPersister artifactDescriptorPersister, EventLogger eventLogger)
        throws RepositoryCreationException, IndexFormatException {
        super(configuration, artifactDescriptorPersister, eventLogger);
        
        this.eventLogger = eventLogger;
        this.antPathMatchingFileSystemSearcher = new AntPathMatchingFileSystemSearcher(configuration.getSearchPattern());

        if (artifactDescriptorPersister.loadArtifacts().isEmpty()) {
            initialiseDepository(configuration.getName(), eventLogger);
        }
    }

    private void initialiseDepository(String repositoryName, EventLogger eventLogger) throws RepositoryCreationException {

            ExternalArtifactStore artifactStore = new ExternalArtifactStore(this.antPathMatchingFileSystemSearcher);
            Set<File> artifacts = artifactStore.getArtifacts();
            ArtifactDescriptorDepository artifactDepository = getDepository();
            for (File artifact : artifacts) {
                try {
                    RepositoryAwareArtifactDescriptor artifactDescriptor = createArtifactDescriptor(artifact);
                    if (artifactDescriptor != null) {
                        artifactDepository.addArtifactDescriptor(artifactDescriptor);
                    }
                } catch (DuplicateArtifactException dae) {
                    LOGGER.warn("Duplicate artifact '{}' discovered in external repository '{}'.", artifact, repositoryName);
                }
            }
            try {
                artifactDepository.persist();
            } catch (IOException ioe) {
                LOGGER.error(String.format("Persisting repository '%s' failed.", repositoryName), ioe);
                eventLogger.log(RepositoryLogEvents.REPOSITORY_INDEX_NOT_PERSISTED, ioe, getName());
                throw new RepositoryCreationException("Failed to persist depository for repository '" + repositoryName + "'", ioe);
            }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RepositoryInfo createMBean() throws JMException {
        return new StandardExternalStorageRepositoryInfo(getName(), getDepository(), this);
    }

    public RepositoryAwareArtifactDescriptor publish(URI uri) throws DuplicateArtifactException {
        if (!"file".equals(uri.getScheme())) {
            LOGGER.error("Uri '{}' not supported for artifact publication to external repository '{}'.", uri, this.getName());
            eventLogger.log(RepositoryLogEvents.ARTIFACT_NOT_PUBLISHED, uri, getName());
            throw new IllegalArgumentException("'" + uri.getScheme()
                + "' is an unsupported scheme for artifact publication. Supported schemes are: 'file'.");
        }
        RepositoryAwareArtifactDescriptor artifactDescriptor = createArtifactDescriptor(new File(uri));
        if (artifactDescriptor==null) {
            LOGGER.error("Uri '{}' not a valid artifact for external repository '{}'.", uri, this.getName());
            eventLogger.log(RepositoryLogEvents.ARTIFACT_NOT_PUBLISHED, uri, getName(), uri.getScheme());
            throw new IllegalArgumentException("'" + uri + "' is not a valid artifact for publication to '" + this.getName() + "'.");
        }
        this.getDepository().addArtifactDescriptor(artifactDescriptor);
        return artifactDescriptor;
    }

    public boolean retract(String type, String name, Version version) {
        RepositoryAwareArtifactDescriptor artifactDescriptor = get(type, name, VersionRange.createExactRange(version));
        if (artifactDescriptor != null) {
            return getDepository().removeArtifactDescriptor(artifactDescriptor);
        } else {
            LOGGER.warn("Artifact (type='{}',name='{}',version='{}') not in repository '{}' and cannot be retracted.", new Object[] { type, name,
                version, this.getName() });
            return false;
        }
    }

    public Set<String> getArtifactLocations(String filename) {
        File rootDir = this.antPathMatchingFileSystemSearcher.getRootDir();
        Set<String> locations = locationsInDirs(rootDir, filename);
        return locations;
    }

    private Set<String> locationsInDirs(File dir, String filename) {
        Set<String> locations = new HashSet<String>();
        String pathHere =  new File(dir,filename).getAbsolutePath();
        if (this.antPathMatchingFileSystemSearcher.matchPath(pathHere)) {
            locations.add(pathHere);
        }
        if (dir.isDirectory()) {
            for (File f : FileSystemUtils.listFiles(dir)) {
                if (f.isDirectory()) {
                    locations.addAll(locationsInDirs(f, filename));
                }
            }
        }
        return locations;
    }
}
