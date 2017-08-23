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

package org.eclipse.virgo.kernel.deployer.core.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.environment.InstallLog;
import org.eclipse.virgo.kernel.osgi.framework.ImportMergeException;
import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyDependenciesException;
import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyLibraryDependenciesException;
import org.eclipse.virgo.util.common.DirectedAcyclicGraph;
import org.eclipse.virgo.util.common.ThreadSafeDirectedAcyclicGraph;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

/**
 */
// TODO Improve the tests to check that the right number of bundles is passed to expandImports
// TODO Improve the tests to check that bundles in scoped plans are processed as a group
public class ImportExpandingTransformerTests {

    private BundleInstallArtifact bundleIinstallArtifact;

    private InstallEnvironment installEnvironment;

    private BundleManifest bundleManifest;

    private org.eclipse.virgo.kernel.osgi.framework.ImportExpander importExpander;

    private InstallLog installLog;

	private DirectedAcyclicGraph<InstallArtifact> dag;

    @Before
    public void setUp() throws Exception {
    		this.dag = new ThreadSafeDirectedAcyclicGraph<InstallArtifact>();
        this.bundleIinstallArtifact = createMock(BundleInstallArtifact.class);
        this.installEnvironment = createMock(InstallEnvironment.class);
        this.importExpander = createMock(org.eclipse.virgo.kernel.osgi.framework.ImportExpander.class);
        this.installLog = createMock(InstallLog.class);
        this.bundleManifest = BundleManifestFactory.createBundleManifest();
    }

    @After
    public void tearDown() {
        resetMocks();
    }

    private void replayMocks() {
        replay(this.bundleIinstallArtifact, this.installEnvironment, this.importExpander, this.installLog);
    }

    private void verifyMocks() {
        verify(this.bundleIinstallArtifact, this.installEnvironment, this.importExpander, this.installLog);
    }

    private void resetMocks() {
        reset(this.bundleIinstallArtifact, this.installEnvironment, this.importExpander, this.installLog);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNormalCase() throws DeploymentException, IOException, ImportMergeException, UnableToSatisfyDependenciesException {

        this.bundleManifest.getImportLibrary().addImportedLibrary("lib");

        expect(this.bundleIinstallArtifact.getBundleManifest()).andReturn(this.bundleManifest);
        expect(this.importExpander.expandImports(isA(List.class))).andReturn(null);
        expect(this.installEnvironment.getInstallLog()).andReturn(this.installLog);
        this.installLog.log(isA(Object.class), isA(String.class), isA(String.class));
        expectLastCall();

        replayMocks();

        ImportExpandingTransformer importExpander = new ImportExpandingTransformer(this.importExpander);
        importExpander.transform(this.dag.createRootNode(bundleIinstallArtifact), installEnvironment);

        verifyMocks();
    }

    @Test
    public void testBundleManifestIOException() throws Exception {

        this.bundleManifest.getImportLibrary().addImportedLibrary("lib");

        expect(this.bundleIinstallArtifact.getBundleManifest()).andThrow(new IOException());
        expect(this.installEnvironment.getInstallLog()).andReturn(this.installLog);
        this.installLog.log(isA(Object.class), isA(String.class), isA(String.class));
        expectLastCall();

        replayMocks();

        ImportExpandingTransformer importExpander = new ImportExpandingTransformer(this.importExpander);
        try {
            importExpander.transform(this.dag.createRootNode(bundleIinstallArtifact), installEnvironment);
        } catch (DeploymentException e) {
            assertTrue(e.getCause() instanceof IOException);
        }

        verifyMocks();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testImportMergeException() throws Exception {

        this.bundleManifest.getImportLibrary().addImportedLibrary("lib");

        expect(this.bundleIinstallArtifact.getBundleManifest()).andReturn(this.bundleManifest);
        expect(this.importExpander.expandImports(isA(List.class))).andThrow(new ImportMergeException("pkg", "src1", "src2"));
        expect(this.installEnvironment.getInstallLog()).andReturn(this.installLog);
        this.installLog.log(isA(Object.class), isA(String.class), isA(String.class), isA(String.class), isA(String.class));
        expectLastCall();

        replayMocks();

        ImportExpandingTransformer importExpander = new ImportExpandingTransformer(this.importExpander);
        try {
            importExpander.transform(this.dag.createRootNode(bundleIinstallArtifact), installEnvironment);
        } catch (DeploymentException e) {
            assertTrue(e.getCause() instanceof ImportMergeException);
        }

        verifyMocks();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUnableToSatisfyDependenciesException() throws Exception {

        this.bundleManifest.getImportLibrary().addImportedLibrary("lib");

        expect(this.bundleIinstallArtifact.getBundleManifest()).andReturn(this.bundleManifest);       
        expect(this.importExpander.expandImports(isA(List.class))).andThrow(
            new UnableToSatisfyLibraryDependenciesException("sym", new Version("0"), "fail"));
        expect(this.installEnvironment.getInstallLog()).andReturn(this.installLog);
        this.installLog.log(isA(Object.class), isA(String.class), isA(String.class), isA(String.class));
        expectLastCall();

        replayMocks();

        ImportExpandingTransformer importExpander = new ImportExpandingTransformer(this.importExpander);
        try {
            importExpander.transform(this.dag.createRootNode(bundleIinstallArtifact), installEnvironment);
        } catch (DeploymentException e) {
            assertTrue(e.getCause() instanceof UnableToSatisfyLibraryDependenciesException);
        }

        verifyMocks();
    }

}
