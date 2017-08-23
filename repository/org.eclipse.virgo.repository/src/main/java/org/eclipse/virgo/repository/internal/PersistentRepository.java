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

package org.eclipse.virgo.repository.internal;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.ArtifactDescriptorPersister;
import org.eclipse.virgo.repository.IndexFormatException;
import org.eclipse.virgo.repository.XmlArtifactDescriptorPersister;
import org.eclipse.virgo.repository.codec.XMLRepositoryCodec;
import org.eclipse.virgo.repository.configuration.PersistentRepositoryConfiguration;

/**
 * An extension of {@link LocalRepository} that creates a ensure that artifact indexes are written out
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public abstract class PersistentRepository extends LocalRepository {

    protected PersistentRepository(PersistentRepositoryConfiguration configuration, EventLogger eventLogger) throws IndexFormatException {
        super(configuration, new XmlArtifactDescriptorPersister(new XMLRepositoryCodec(), configuration.getName(), configuration.getIndexLocation()),
            eventLogger);
    }
    protected PersistentRepository(PersistentRepositoryConfiguration configuration, ArtifactDescriptorPersister artifactDescriptorPersister, EventLogger eventLogger) throws IndexFormatException {
        super(configuration, artifactDescriptorPersister, eventLogger);
    }
}
