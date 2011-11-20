/*******************************************************************************
 * Copyright (c) 2011 EclipseSource
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - Bug 358442 Change InstallArtifact graph from a tree to a DAG
 *******************************************************************************/

package org.eclipse.virgo.kernel.install.artifact.internal;

import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactGraphFactory;
import org.eclipse.virgo.util.common.DirectedAcyclicGraph;
import org.eclipse.virgo.util.common.GraphNode;

/**
 * {@link AbstractArtifactGraphFactory} is a base class for implementations of
 * {@link InstallArtifactGraphFactory}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public abstract class AbstractArtifactGraphFactory implements
		InstallArtifactGraphFactory {

	private final DirectedAcyclicGraph<InstallArtifact> dag;

	public AbstractArtifactGraphFactory(
			DirectedAcyclicGraph<InstallArtifact> dag) {
		this.dag = dag;
	}

	protected GraphNode<InstallArtifact> constructInstallGraph(
			InstallArtifact rootArtifact) {
		return this.dag.createRootNode(rootArtifact);
	}

}