/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   EclipseSource - Bug 358442 Change InstallArtifact graph from a tree to a DAG
 *******************************************************************************/

package org.eclipse.virgo.kernel.install.artifact;

import java.util.Set;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.nano.core.AbortableSignal;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.util.common.GraphNode;
import org.osgi.framework.Version;

/**
 * An {@link InstallArtifact} is a single node in an install tree.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementation <strong>must</strong> be thread-safe.
 * 
 */
public interface InstallArtifact {

	/**
	 * The possible states of an artifact.
	 */
	public enum State {
		INITIAL, INSTALLING, INSTALLED, RESOLVING, RESOLVED, STARTING, ACTIVE, STOPPING, UNINSTALLING, UNINSTALLED
	}

	/**
	 * Returns the type of the artifact.
	 * 
	 * @return the artifact's type.
	 */
	String getType();

	/**
	 * Returns the name of the artifact.
	 * 
	 * @return the artifact's name.
	 */
	String getName();

	/**
	 * Returns the version of the artifact.
	 * 
	 * @return the artifact's version.
	 */
	Version getVersion();

	/**
	 * Returns the name of the scope in which this <code>InstallArtifact</code>
	 * resides, or <code>null</code> if it is not scoped.
	 * 
	 * @return the artifact's scope name
	 */
	String getScopeName();

	/**
	 * Gets the {@link State} of this {@link InstallArtifact}.
	 * 
	 * @return a non-<code>null</code> <code>State</code>
	 */
	State getState();

	/**
	 * Starts this {@link InstallArtifact}. Returns before any asynchronous
	 * processing has necessarily completed.
	 * <p/>
	 * Equivalent to calling {@link InstallArtifact#start(AbortableSignal)
	 * start(null)}.
	 * 
	 * @throws DeploymentException
	 */
	void start() throws DeploymentException;

	/**
	 * Starts this {@link InstallArtifact} and drives the given
	 * {@link AbortableSignal} when the start, including any asynchronous
	 * processing, completes either successfully or unsuccessfully.
	 * <p/>
	 * If the start does not involve asynchronous processing, drives the given
	 * <code>Signal</code> before returning.
	 * <p/>
	 * Note that the given <code>Signal</code> may be driven before this method
	 * returns, after the method has returned, or possibly never if there is
	 * asynchronous processing which never completes. The caller must ensure
	 * that the given <code>Signal</code> is ready to be driven <i>before</i>
	 * calling this method.
	 * <p/>
	 * If the caller is not interested in being signalled, it should pass a
	 * <code>null</code> <code>Signal</code>.
	 * <p/>
	 * Note: this method behaves as specified above when called multiple times
	 * on the same <code>InstallArtifact</code> with any combination of the same
	 * or distinct or <code>null</code> <code>Signals</code>.
	 * 
	 * @param signal
	 *            a <code>AbortableSignal</code> that is ready to be driven or
	 *            <code>null</code> if signalling is not required
	 * @throws DeploymentException
	 */
	void start(AbortableSignal signal) throws DeploymentException;

	/**
	 * Stops this {@link InstallArtifact}. If the <code>InstallArtifact</code>
	 * is already stopped, do nothing.
	 * 
	 * @throws DeploymentException
	 *             if the operation fails
	 */
	void stop() throws DeploymentException;

	/**
	 * Uninstalls this {@link InstallArtifact}. If the
	 * <code>InstallArtifact</code> is already uninstalled, do nothing.
	 * 
	 * @throws DeploymentException
	 *             if the operation fails
	 */
	void uninstall() throws DeploymentException;

	/**
	 * Returns the <code>ArtifactFS</code> for this artifact
	 * 
	 * @return the <code>ArtifactFS</code>
	 */
	ArtifactFS getArtifactFS();

	/**
	 * Attempts to refresh this {@link InstallArtifact}. Returns
	 * <code>true</code> if and only if this has completed successfully.
	 * 
	 * @return <code>true</code> if and only if the operation completed
	 *         successfully
	 * @throws DeploymentException
	 */
	boolean refresh() throws DeploymentException;

	/**
	 * Associates the property with the given name and value with this
	 * {@link InstallArtifact}. Properties may be set by participants in the
	 * artifact's installation. Once the artifact has been installed, an MBean
	 * representing it is exported and these properties will be available via
	 * that MBean.
	 * 
	 * @param name
	 *            the property name
	 * @param value
	 *            the property value
	 * @return the property's previous value or <code>null</code> if there was
	 *         no such property
	 */
	String setProperty(@NonNull String name, @NonNull String value);

	/**
	 * Returns the property with the given name associated with this
	 * {@link InstallArtifact}. If there is no associated property with the
	 * given name, returns <code>null</code>.
	 * 
	 * @param name
	 *            the property name
	 * @return the property value or <code>null</code> if there is no property
	 *         with the given name
	 */
	String getProperty(@NonNull String name);

	/**
	 * Returns the names of the properties that are associated with this
	 * {@link InstallArtifact}.
	 * 
	 * @return a set of property names
	 */
	Set<String> getPropertyNames();

	/**
	 * Returns the local name of the repository from which the artifact was
	 * installed, or null if not from a repository.
	 * 
	 * @return the local name of the repository whence the artifact came
	 */
	String getRepositoryName();

	/**
	 * Returns the install graph rooted in this {@link InstallArtifact}.
	 * 
	 * @return this artifact's graph
	 */
	GraphNode<InstallArtifact> getGraph();
}
