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
import org.eclipse.virgo.util.common.DirectedAcyclicGraph;
import org.eclipse.virgo.util.common.ThreadSafeDirectedAcyclicGraph;

/**
 * {@link InstallArtifactDAGSingletonFactory} is used to create the graph of
 * {@link InstallArtifact InstallArtifacts}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
final class InstallArtifactDAGSingletonFactory {

	private static final DirectedAcyclicGraph<InstallArtifact> DAG = new ThreadSafeDirectedAcyclicGraph<InstallArtifact>();

	public static DirectedAcyclicGraph<InstallArtifact> createInstance() {
		return DAG;
	}

}
