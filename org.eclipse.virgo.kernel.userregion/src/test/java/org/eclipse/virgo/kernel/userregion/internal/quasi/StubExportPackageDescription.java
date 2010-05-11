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

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.osgi.framework.Version;

/**
 */
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
    public BundleDescription getExporter() {
        return this.exporter;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRoot() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    public BundleDescription getSupplier() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public Version getVersion() {
        return this.version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }
    
    public void setExporter(BundleDescription exporter) {
        this.exporter = exporter;
    }

}
