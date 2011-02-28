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

package org.eclipse.virgo.kernel.shell.state.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

/**
 * <p>
 * KernelRegionFauxQuasiLiveBundle is a fake bundle that represents bundles not in the user region.
 * </p>
 *
 */
public class KernelRegionFauxQuasiLiveBundle implements Bundle{
    
    private final long bundleId;
    private final Version version;
    private final String symbolicName;
    
    public KernelRegionFauxQuasiLiveBundle(long bundleId, Version version, String symbolicName) {
        this.bundleId = bundleId;
        this.version = version;
        this.symbolicName = symbolicName;
    }

    @Override
    public int compareTo(Bundle o) {
        return 0;
    }

    @Override
    public int getState() {
        return 0x00000020;
    }

    @Override
    public void start(int options) throws BundleException {
        // no-op
    }

    @Override
    public void start() throws BundleException {
        // no-op
    }

    @Override
    public void stop(int options) throws BundleException {
        // no-op
    }

    @Override
    public void stop() throws BundleException {
        // no-op
    }

    @Override
    public void update(InputStream input) throws BundleException {
        // no-op
    }

    @Override
    public void update() throws BundleException {
        // no-op
    }

    @Override
    public void uninstall() throws BundleException {
        // no-op
    }

    @Override
    public Dictionary<String, String> getHeaders() {
        return new Hashtable<String, String>();
    }

    @Override
    public long getBundleId() {
        return this.bundleId;
    }

    @Override
    public String getLocation() {
        return "Kernel Region";
    }

    @Override
    public ServiceReference<?>[] getRegisteredServices() {
        return new ServiceReference[0];
    }

    @Override
    public ServiceReference<?>[] getServicesInUse() {
        return new ServiceReference[0];
    }

    @Override
    public boolean hasPermission(Object permission) {
        return false;
    }

    @Override
    public URL getResource(String name) {
        return null;
    }

    @Override
    public Dictionary<String, String> getHeaders(String locale) {
        return new Hashtable<String, String>();
    }

    @Override
    public String getSymbolicName() {
        return this.symbolicName;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException("Kernel Region Bundle");
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return null;
    }

    @Override
    public Enumeration<String> getEntryPaths(String path) {
        return null;
    }

    @Override
    public URL getEntry(String path) {
        return null;
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    @Override
    public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
        return null;
    }

    @Override
    public BundleContext getBundleContext() {
        return null;
    }

    @Override
    public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int signersType) {
        return null;
    }

    @Override
    public Version getVersion() {
        return this.version;
    }

    @Override
    public <A> A adapt(Class<A> type) {
        return null;
    }

    @Override
    public File getDataFile(String filename) {
        return null;
    }

}
