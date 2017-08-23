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
import java.io.Writer;
import java.net.URL;
import java.util.Dictionary;
import java.util.List;

import org.osgi.framework.Version;

import org.eclipse.virgo.util.osgi.manifest.BundleActivationPolicy;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleSymbolicName;
import org.eclipse.virgo.util.osgi.manifest.DynamicImportPackage;
import org.eclipse.virgo.util.osgi.manifest.ExportPackage;
import org.eclipse.virgo.util.osgi.manifest.FragmentHost;
import org.eclipse.virgo.util.osgi.manifest.ImportBundle;
import org.eclipse.virgo.util.osgi.manifest.ImportLibrary;
import org.eclipse.virgo.util.osgi.manifest.ImportPackage;
import org.eclipse.virgo.util.osgi.manifest.RequireBundle;

/**
 * {@link DescopingBundleManifest} is a wrapper of a {@link BundleManifest} that reverses the effects of scoping. In the
 * first instance only the effects on the bundle symbolic are reversed.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class DescopingBundleManifest implements BundleManifest {

    private final BundleManifest wrappedManifest;

    DescopingBundleManifest(BundleManifest bundleManifest) {
        this.wrappedManifest = bundleManifest;
    }

    /**
     * {@inheritDoc}
     */
    public BundleActivationPolicy getBundleActivationPolicy() {
        return this.wrappedManifest.getBundleActivationPolicy();
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getBundleClasspath() {
        return this.wrappedManifest.getBundleClasspath();
    }

    /**
     * {@inheritDoc}
     */
    public String getBundleDescription() {
        return this.wrappedManifest.getBundleDescription();
    }

    /**
     * {@inheritDoc}
     */
    public int getBundleManifestVersion() {
        return this.wrappedManifest.getBundleManifestVersion();
    }

    /**
     * {@inheritDoc}
     */
    public String getBundleName() {
        return this.wrappedManifest.getBundleName();
    }

    /**
     * {@inheritDoc}
     */
    public BundleSymbolicName getBundleSymbolicName() {
        return new DescopingBundleSymbolicName(this.wrappedManifest.getBundleSymbolicName(), this.wrappedManifest.getModuleScope());
    }

    /**
     * {@inheritDoc}
     */
    public URL getBundleUpdateLocation() {
        return this.wrappedManifest.getBundleUpdateLocation();
    }

    /**
     * {@inheritDoc}
     */
    public Version getBundleVersion() {
        return this.wrappedManifest.getBundleVersion();
    }

    /**
     * {@inheritDoc}
     */
    public DynamicImportPackage getDynamicImportPackage() {
        return this.wrappedManifest.getDynamicImportPackage();
    }

    /**
     * {@inheritDoc}
     */
    public ExportPackage getExportPackage() {
        return this.wrappedManifest.getExportPackage();
    }

    /**
     * {@inheritDoc}
     */
    public FragmentHost getFragmentHost() {
        return this.wrappedManifest.getFragmentHost();
    }

    /**
     * {@inheritDoc}
     */
    public String getHeader(String name) {
        return this.wrappedManifest.getHeader(name);
    }

    /**
     * {@inheritDoc}
     */
    public ImportBundle getImportBundle() {
        return this.wrappedManifest.getImportBundle();
    }

    /**
     * {@inheritDoc}
     */
    public ImportLibrary getImportLibrary() {
        return this.wrappedManifest.getImportLibrary();
    }

    /**
     * {@inheritDoc}
     */
    public ImportPackage getImportPackage() {
        return this.wrappedManifest.getImportPackage();
    }

    /**
     * {@inheritDoc}
     */
    public String getModuleScope() {
        return this.wrappedManifest.getModuleScope();
    }

    /**
     * {@inheritDoc}
     */
    public String getModuleType() {
        return this.wrappedManifest.getModuleType();
    }

    /**
     * {@inheritDoc}
     */
    public RequireBundle getRequireBundle() {
        return this.wrappedManifest.getRequireBundle();
    }

    /**
     * {@inheritDoc}
     */
    public void setBundleDescription(String bundleDescription) {
        this.wrappedManifest.setBundleDescription(bundleDescription);
    }

    /**
     * {@inheritDoc}
     */
    public void setBundleManifestVersion(int bundleManifestVersion) {
        this.wrappedManifest.setBundleManifestVersion(bundleManifestVersion);
    }

    /**
     * {@inheritDoc}
     */
    public void setBundleName(String bundleName) {
        this.wrappedManifest.setBundleName(bundleName);
    }

    /**
     * {@inheritDoc}
     */
    public void setBundleUpdateLocation(URL bundleUpdateLocation) {
        this.wrappedManifest.setBundleUpdateLocation(bundleUpdateLocation);
    }

    /**
     * {@inheritDoc}
     */
    public void setBundleVersion(Version bundleVersion) {
        this.wrappedManifest.setBundleVersion(bundleVersion);
    }

    /**
     * {@inheritDoc}
     */
    public void setHeader(String name, String value) {
        this.wrappedManifest.setHeader(name, value);
    }

    /**
     * {@inheritDoc}
     */
    public void setModuleScope(String moduleScope) {
        this.wrappedManifest.setModuleScope(moduleScope);
    }

    /**
     * {@inheritDoc}
     */
    public void setModuleType(String moduleType) {
        this.wrappedManifest.setModuleType(moduleType);
    }

    /**
     * {@inheritDoc}
     */
    public Dictionary<String, String> toDictionary() {
        return this.wrappedManifest.toDictionary();
    }

    /**
     * {@inheritDoc}
     */
    public void write(Writer writer) throws IOException {
        this.wrappedManifest.write(writer);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.wrappedManifest.toString();
    }

}
