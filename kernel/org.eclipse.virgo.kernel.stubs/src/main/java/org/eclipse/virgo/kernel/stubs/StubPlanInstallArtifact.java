/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   EclipseSource - Bug 358442 Change InstallArtifact graph from a tree to a DAG
 *******************************************************************************/

package org.eclipse.virgo.kernel.stubs;

import java.util.List;

import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.eclipse.virgo.kernel.artifact.plan.PlanDescriptor.Provisioning;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;

import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.util.common.DirectedAcyclicGraph;

public class StubPlanInstallArtifact extends StubGraphAssociableInstallArtifact implements PlanInstallArtifact {

    public StubPlanInstallArtifact() {
		super();
	}

	public StubPlanInstallArtifact(DirectedAcyclicGraph<InstallArtifact> dag) {
    		super(null, null, null, null, null, dag);
	}

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

    public Provisioning getProvisioning() {
        return Provisioning.INHERIT;
    }
}
