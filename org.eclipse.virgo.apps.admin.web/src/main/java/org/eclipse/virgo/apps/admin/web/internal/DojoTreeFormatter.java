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

package org.eclipse.virgo.apps.admin.web.internal;

import java.util.List;

import org.eclipse.virgo.kernel.shell.model.helper.ArtifactAccessor;
import org.eclipse.virgo.kernel.shell.model.helper.ArtifactAccessorPointer;

/**
 * <p>
 * DojoTreeFormatter knows how to format data from the RAM in to a friendly dojo format.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * DojoTreeFormatter is thread safe
 *
 */
public interface DojoTreeFormatter {

    /**
     * Return a description of all the artifact types in the system that are user installed
     * @param types list of type names to describe
     * @return description
     */
    public String formatTypes(final List<String> types);

    /**
     * Return a description of all the artifacts supplied the system that are user installed and of the given type
     * @param parent name of parent
     * @param artifacts to describe
     * @return description
     */
    public String formatArtifactsOfType(final String parent, final List<ArtifactAccessorPointer> artifacts);

    /**
     * Return detailed information on the given artifact
     * @param parent name of parent
     * @param artifact to describe
     * @return description
     */
    public String formatArtifactDetails(final String parent, final ArtifactAccessor artifact);

}
