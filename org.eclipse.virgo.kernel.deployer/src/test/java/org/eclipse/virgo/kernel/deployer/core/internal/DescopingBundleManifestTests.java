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

package org.eclipse.virgo.kernel.deployer.core.internal;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

import static org.easymock.EasyMock.*;

import org.eclipse.virgo.kernel.deployer.core.internal.DescopingBundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

public class DescopingBundleManifestTests {

    private BundleManifest descopingBundleManifest;

    private BundleManifest mockBundleManifest;

    @Before
    public void setup() {
        this.mockBundleManifest = createMock(BundleManifest.class);
        this.descopingBundleManifest = new DescopingBundleManifest(this.mockBundleManifest);
    }

    @Test
    public void testBundleActivationPolicy() {
        expect(this.mockBundleManifest.getBundleActivationPolicy()).andReturn(null);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.getBundleActivationPolicy();
        verify(this.mockBundleManifest);
    }

    @Test
    public void testBundleClasspath() {
        expect(this.mockBundleManifest.getBundleClasspath()).andReturn(null);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.getBundleClasspath();
        verify(this.mockBundleManifest);
    }

    @Test
    public void testBundleDescription() {
        expect(this.mockBundleManifest.getBundleDescription()).andReturn(null);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.getBundleDescription();
        verify(this.mockBundleManifest);
    }

    @Test
    public void testBundleManifestVersion() {
        expect(this.mockBundleManifest.getBundleManifestVersion()).andReturn(0);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.getBundleManifestVersion();
        verify(this.mockBundleManifest);
    }

    @Test
    public void testBundleName() {
        expect(this.mockBundleManifest.getBundleName()).andReturn(null);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.getBundleName();
        verify(this.mockBundleManifest);
    }

    @Test
    public void testBundleSymbolicName() {
        expect(this.mockBundleManifest.getBundleSymbolicName()).andReturn(null);
        expect(this.mockBundleManifest.getModuleScope()).andReturn(null);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.getBundleSymbolicName();
        verify(this.mockBundleManifest);
    }

    @Test
    public void testBundleUpdateLocation() {
        expect(this.mockBundleManifest.getBundleUpdateLocation()).andReturn(null);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.getBundleUpdateLocation();
        verify(this.mockBundleManifest);
    }

    @Test
    public void testBundleVersion() {
        expect(this.mockBundleManifest.getBundleVersion()).andReturn(null);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.getBundleVersion();
        verify(this.mockBundleManifest);
    }

    @Test
    public void testDynamicImportPackage() {
        expect(this.mockBundleManifest.getDynamicImportPackage()).andReturn(null);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.getDynamicImportPackage();
        verify(this.mockBundleManifest);
    }

    @Test
    public void testExportPackage() {
        expect(this.mockBundleManifest.getExportPackage()).andReturn(null);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.getExportPackage();
        verify(this.mockBundleManifest);
    }

    @Test
    public void testFragmentHost() {
        expect(this.mockBundleManifest.getFragmentHost()).andReturn(null);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.getFragmentHost();
        verify(this.mockBundleManifest);
    }

    @Test
    public void testHeader() {
        expect(this.mockBundleManifest.getHeader(isA(String.class))).andReturn(null);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.getHeader("");
        verify(this.mockBundleManifest);
    }

    @Test
    public void testImportBundle() {
        expect(this.mockBundleManifest.getImportBundle()).andReturn(null);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.getImportBundle();
        verify(this.mockBundleManifest);
    }

    @Test
    public void testImportLibrary() {
        expect(this.mockBundleManifest.getImportLibrary()).andReturn(null);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.getImportLibrary();
        verify(this.mockBundleManifest);
    }

    @Test
    public void testImportPackage() {
        expect(this.mockBundleManifest.getImportPackage()).andReturn(null);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.getImportPackage();
        verify(this.mockBundleManifest);
    }

    @Test
    public void testModuleScope() {
        expect(this.mockBundleManifest.getModuleScope()).andReturn(null);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.getModuleScope();
        verify(this.mockBundleManifest);
    }

    @Test
    public void testModuleType() {
        expect(this.mockBundleManifest.getModuleType()).andReturn(null);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.getModuleType();
        verify(this.mockBundleManifest);
    }

    @Test
    public void testRequireBundle() {
        expect(this.mockBundleManifest.getRequireBundle()).andReturn(null);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.getRequireBundle();
        verify(this.mockBundleManifest);
    }

    @Test
    public void testSetBundleDescription() {
        this.mockBundleManifest.setBundleDescription(isA(String.class));
        expectLastCall();
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.setBundleDescription("");
        verify(this.mockBundleManifest);
    }

    @Test
    public void testSetBundleManifestVersion() {
        this.mockBundleManifest.setBundleManifestVersion(eq(0));
        expectLastCall();
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.setBundleManifestVersion(0);
        verify(this.mockBundleManifest);
    }

    @Test
    public void testSetBundleName() {
        this.mockBundleManifest.setBundleName(isA(String.class));
        expectLastCall();
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.setBundleName("");
        verify(this.mockBundleManifest);
    }

    @Test
    public void testSetBundleUpdateLocation() throws MalformedURLException {
        this.mockBundleManifest.setBundleUpdateLocation(isA(URL.class));
        expectLastCall();
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.setBundleUpdateLocation(new URL("http://blah"));
        verify(this.mockBundleManifest);
    }

    @Test
    public void testSetBundleVersion() {
        this.mockBundleManifest.setBundleVersion(isA(Version.class));
        expectLastCall();
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.setBundleVersion(Version.emptyVersion);
        verify(this.mockBundleManifest);
    }

    @Test
    public void testSetHeader() {
        this.mockBundleManifest.setHeader(isA(String.class), isA(String.class));
        expectLastCall();
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.setHeader("", "");
        verify(this.mockBundleManifest);
    }

    @Test
    public void testSetModuleScope() {
        this.mockBundleManifest.setModuleScope(isA(String.class));
        expectLastCall();
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.setModuleScope("");
        verify(this.mockBundleManifest);
    }

    @Test
    public void testSetModuleType() {
        this.mockBundleManifest.setModuleType(isA(String.class));
        expectLastCall();
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.setModuleType("");
        verify(this.mockBundleManifest);
    }

    @Test
    public void testToDictionary() {
        expect(this.mockBundleManifest.toDictionary()).andReturn(null);
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.toDictionary();
        verify(this.mockBundleManifest);
    }
    
    @Test
    public void testWrite() throws IOException {
        this.mockBundleManifest.write(isA(Writer.class));
        expectLastCall();
        replay(this.mockBundleManifest);
        this.descopingBundleManifest.write(new StringWriter());
        verify(this.mockBundleManifest);
    }

}
