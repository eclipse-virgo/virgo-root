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

package org.eclipse.virgo.medic.log;

import java.io.File;

/**
 * A <code>LoggingConfigurationPublisher</code> is used to publish {@link LoggingConfiguration} into the service
 * registry. <code>LoggingConfiguration</code> instances in the service registry are referenced by bundles using the
 * <code>Medic-LoggingConfiguration</code> manifest header.
 */
public interface LoggingConfigurationPublisher {

    /**
     * Publishes the configuration in the supplied <code>File</code> as a <code>LoggingConfiguration</code> instance,
     * identified with the supplied id. The published configuration can then be referenced by a bundle using the
     * <code>Medic-LoggingConfiguration</code> manifest header with a value equal to the supplied id.
     * 
     * @param configuration The configuration to be published
     * @param id The identifier to be applied to the configuration when its published
     * 
     * @throws ConfigurationPublicationFailedException if the publication of the configuration fails
     */
    void publishConfiguration(File configuration, String id) throws ConfigurationPublicationFailedException;
}
