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

package org.eclipse.virgo.kernel.stubs;

import java.util.List;

import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;

import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;

public class StubPlanInstallArtifact extends StubInstallArtifact implements PlanInstallArtifact {

    public String getType() {
        return "plan";
    }

    public boolean isAtomic() {
        return false;
    }

    public boolean isScoped() {
        return false;
    }

    public boolean refresh(String symbolicName) throws DeploymentException {
        return false;
    }

    public boolean refreshScope() {
        return false;
    }

    public List<ArtifactSpecification> getArtifactSpecifications() {
        return null;
    }
}
