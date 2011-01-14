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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.core.AbortableSignal;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.util.common.ThreadSafeArrayListTree;
import org.eclipse.virgo.util.common.Tree;
import org.osgi.framework.Version;

public class StubInstallArtifact implements InstallArtifact {

    private final ArtifactFS artifactFS;

    private final String name;

    private final Version version;

    private final String type;

    private final Map<String, String> properties = new HashMap<String, String>();

    private final Tree<InstallArtifact> tree;
    
    private final String scopeName;

    public StubInstallArtifact() {
        this(null, null, null, null);
    }

    public StubInstallArtifact(String type) {
        this(null, type, null, null);
    }

    public StubInstallArtifact(ArtifactFS artifactFS, String name, Version version) {
        this(artifactFS, null, name, version);
    }

    public StubInstallArtifact(ArtifactFS artifactFS, String type, String name, Version version) {
        this(artifactFS, type, name, version, new InstallArtifact[0]);
    }
    
    public StubInstallArtifact(ArtifactFS artifactFS, String type, String name, Version version, String scopeName) {
        this(artifactFS, type, name, version, scopeName, new InstallArtifact[0]);
    }

    public StubInstallArtifact(ArtifactFS artifactFS, String type, String name, Version version, InstallArtifact... children) {
        this(artifactFS, type, name, version, null, children);
    } 
    
    public StubInstallArtifact(ArtifactFS artifactFS, String type, String name, Version version, String scopeName, InstallArtifact... children) {
        this.type = type == null ? "stub" : type;
        this.name = name == null ? "the-stub" : name;
        this.version = version == null ? Version.emptyVersion : version;
        this.scopeName = scopeName;

        this.artifactFS = artifactFS;

        this.tree = new ThreadSafeArrayListTree<InstallArtifact>(this);

        for (InstallArtifact child : children) {
            this.tree.addChild(new ThreadSafeArrayListTree<InstallArtifact>(child));
        }
    }

    public ArtifactFS getArtifactFS() {
        return this.artifactFS;
    }

    public String getName() {
        return this.name;
    }

    public State getState() {
        throw new UnsupportedOperationException();
    }

    public String getType() {
        return this.type;
    }

    public Version getVersion() {
        return this.version;
    }
    
    public String getScopeName() {
        return this.scopeName;
    }

    public void stop() throws DeploymentException {
    }

    public void uninstall() throws DeploymentException {
    }

    public boolean refresh() {
        return false;
    }

    public String getProperty(@NonNull String name) {
        return this.properties.get(name);
    }

    public Set<String> getPropertyNames() {
        HashSet<String> propertyNames = new HashSet<String>(this.properties.keySet());
        return Collections.unmodifiableSet(propertyNames);
    }

    public String setProperty(String name, String value) {
        return this.properties.put(name, value);
    }

    public String getRepositoryName() {
        throw new UnsupportedOperationException();
    }

    public Tree<InstallArtifact> getTree() {
        return this.tree;
    }

    public void start() throws DeploymentException {
        start(null);
    }

    public void start(AbortableSignal signal) throws DeploymentException {
        if (signal != null) {
            signal.signalSuccessfulCompletion();
        }
    }

}
