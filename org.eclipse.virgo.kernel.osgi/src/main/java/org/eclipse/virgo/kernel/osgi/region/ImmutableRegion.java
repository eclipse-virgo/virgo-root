/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region;

import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.osgi.framework.BundleContext;

/**
 * TODO Document ImmutableRegion
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * TODO Document concurrent semantics of ImmutableRegion
 *
 */
public class ImmutableRegion implements Region {

    private final String name;

    private final BundleContext bundleContext;

    private final RegionPackageImportPolicy regionPackageImportPolicy;

    public ImmutableRegion(String name, @NonNull BundleContext bundleContext, @NonNull RegionPackageImportPolicy regionPackageImportPolicy) {
        this.name = name;
        this.bundleContext = bundleContext;
        this.regionPackageImportPolicy = regionPackageImportPolicy;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public BundleContext getBundleContext() {
        return this.bundleContext;
    }

    @Override
    public RegionPackageImportPolicy getRegionPackageImportPolicy() {
        return this.regionPackageImportPolicy;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bundleContext == null) ? 0 : bundleContext.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ImmutableRegion other = (ImmutableRegion) obj;
        if (bundleContext == null) {
            if (other.bundleContext != null)
                return false;
        } else if (!bundleContext.equals(other.bundleContext))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}