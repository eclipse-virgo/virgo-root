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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiRequiredBundle;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * {@link StandardQuasiRequiredBundle} is the default implementation of {@link QuasiRequiredBundle}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class StandardQuasiRequiredBundle implements QuasiRequiredBundle {

    private final BundleSpecification bundleSpecification;

    private final QuasiBundle requiringBundle;

    public StandardQuasiRequiredBundle(BundleSpecification bundleSpecification, QuasiBundle requiringBundle) {
        this.bundleSpecification = bundleSpecification;
        this.requiringBundle = requiringBundle;
    }

    /**
     * {@inheritDoc}
     */
    public String getRequiredBundleName() {
        return this.bundleSpecification.getName();
    }

    /**
     * {@inheritDoc}
     */
    public VersionRange getVersionConstraint() {
        org.eclipse.osgi.service.resolver.VersionRange resolverVersionRange = this.bundleSpecification.getVersionRange();
        VersionRange versionRange;
        if (resolverVersionRange == null) {
            versionRange = new VersionRange(null); // The range of all possible versions
        } else {
            versionRange = new VersionRange(resolverVersionRange.toString());
        }
        return versionRange;
    }

    /**
     * {@inheritDoc}
     */
    public QuasiBundle getRequiringBundle() {
        return this.requiringBundle;
    }

    /**
     * {@inheritDoc}
     */
    public QuasiBundle getProvider() {
        if (isResolved()) {
            BundleDescription supplier = this.bundleSpecification.getSupplier().getSupplier();
			return new StandardQuasiBundle(supplier, null, this.requiringBundle.getRegion().getRegionDigraph().getRegion(supplier.getBundleId()));
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isResolved() {
        return this.bundleSpecification.isResolved();
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getAttributes() {
        Map<String, Object> attributes = new HashMap<String, Object>();
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getDirectives() {
        Map<String, Object> directives = new HashMap<String, Object>();
        return Collections.unmodifiableMap(directives);
    }

}
