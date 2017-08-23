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

package org.eclipse.virgo.kernel.install.artifact.internal;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.internal.InstallArtifactRefreshHandler;


/**
 */
public class StubInstallArtifactRefreshHandler implements InstallArtifactRefreshHandler {
    
    private final List<InstallArtifact> refreshedArtifacts = new ArrayList<InstallArtifact>();
    
    private Map<InstallArtifact, Boolean> refreshOutcomes = new HashMap<InstallArtifact, Boolean>();

    /** 
     * {@inheritDoc}
     */
    public boolean refresh(InstallArtifact installArtifact) {
        Boolean refreshed = this.refreshOutcomes.get(installArtifact);
        
        if (refreshed == null) {
            throw new IllegalStateException("No refresh outcome set for install artifact '" + installArtifact + "'");
        }
        
        this.refreshedArtifacts.add(installArtifact);
        
        return refreshed;
    }
    
    public void setRefreshOutcome(InstallArtifact artifact, boolean refreshSuccessful) {
        this.refreshOutcomes.put(artifact, refreshSuccessful);
    }
    
    public void assertRefreshed(InstallArtifact installArtifact) {
        assertTrue(this.refreshedArtifacts.contains(installArtifact));
    }
}
