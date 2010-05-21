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

package org.eclipse.virgo.apps.admin.web.stubs;

import java.util.List;

import org.eclipse.virgo.apps.admin.web.internal.DojoTreeFormatter;
import org.eclipse.virgo.kernel.shell.model.helper.ArtifactAccessor;
import org.eclipse.virgo.kernel.shell.model.helper.ArtifactAccessorPointer;

/**
 */
public final class StubDojoTreeFormatter implements DojoTreeFormatter {

    /**
     * {@inheritDoc}
     */
    public String formatArtifactDetails(String parent, ArtifactAccessor artifact) {
        StringBuilder builder = new StringBuilder();
        builder.append(parent);        
        builder.append(artifact.getType());
        builder.append(artifact.getName());
        builder.append(artifact.getVersion());
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String formatArtifactsOfType(String parent, List<ArtifactAccessorPointer> artifacts) {
        StringBuilder builder = new StringBuilder();
        builder.append(parent);
        for(ArtifactAccessorPointer artifact :artifacts) {
            builder.append(artifact.getType());
            builder.append(artifact.getName());
            builder.append(artifact.getVersion());
            builder.append(artifact.getState());
        }
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String formatTypes(List<String> types) {
        StringBuilder builder = new StringBuilder();
        for(String type :types) {
            builder.append(type);
        }
        return builder.toString();
    }
}
