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

package org.eclipse.virgo.kernel.userregion.internal.quasi;

import java.util.Map;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleCapability;

public class StubExportPackageDescription extends StubParameterised implements ExportPackageDescription {

    private String name;
    
    private Version version;

    private BundleDescription exporter;

    public StubExportPackageDescription(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BundleDescription getExporter() {
        return this.exporter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRoot() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BundleDescription getSupplier() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Version getVersion() {
        return this.version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }
    
    public void setExporter(BundleDescription exporter) {
        this.exporter = exporter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getDeclaredDirectives() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getDeclaredAttributes() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BundleCapability getCapability() {
        throw new UnsupportedOperationException();
    }

	@Override
	public Object getUserObject() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setUserObject(Object arg0) {
		throw new UnsupportedOperationException();
	}

}
