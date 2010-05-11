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

package org.eclipse.virgo.kernel.install.artifact.internal.scoping;

import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;


/**
 * <code>ArifactIdentityScoper</code> is used to scope {@link ArtifactIdentity} instances.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public class ArtifactIdentityScoper {
    
    private static final String SCOPE_SEPARATOR = "-";
    
    /**
     * Scopes the supplied <code>ArtifactIdentity</code>.
     * 
     * @param artifactIdentity The <code>ArtifactIdentity</code> to scope
     * @return The scoped <code>ArtifactIdentity</code>
     */
    public static ArtifactIdentity scopeArtifactIdentity(ArtifactIdentity artifactIdentity) {
        
        String scopeName = artifactIdentity.getScopeName();
        
        if (scopeName != null && !ArtifactIdentityDeterminer.CONFIGURATION_TYPE.equals(artifactIdentity.getType())) {
            String scopedName = scopeName + SCOPE_SEPARATOR + artifactIdentity.getName();
            return new ArtifactIdentity(artifactIdentity.getType(), scopedName, artifactIdentity.getVersion(), scopeName);
        } else {
            return artifactIdentity;
        }
    }
    
    /**
     * Returns the unscoped name of the supplied <code>identity</code>.
     * @param identity The <code>ArtifactIdentity</code> for which the unscoped name is required
     * @return The unscoped name
     */
    public static String getUnscopedName(ArtifactIdentity identity) {
        String scopeName = identity.getScopeName();
        String name = identity.getName();

        if (scopeName != null && name.length() > (scopeName.length() + SCOPE_SEPARATOR.length())) {
            return name.substring(scopeName.length() + 1);
        }

        return name;
    }
}
