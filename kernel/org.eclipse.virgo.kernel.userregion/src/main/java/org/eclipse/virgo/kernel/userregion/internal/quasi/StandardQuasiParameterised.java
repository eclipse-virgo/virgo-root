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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.VersionRange;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiParameterised;
import org.eclipse.virgo.util.common.StringUtils;

/**
 * {@link StandardQuasiParameterised} is the default implementation of {@link QuasiParameterised}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public class StandardQuasiParameterised implements QuasiParameterised {

    private static final String VERSION_ATTRIBUTE = "version";
    
    private static final String BUNDLE_SYMBOLIC_NAME_ATTRIBUTE = "bundle-symbolic-name";
    
    private static final String BUNDLE_VERSION_ATTRIBUTE = "bundle-version";
    
    private final Map<String, Object> attributes;

    private final Map<String, Object> directives;

    
    /**
     * Will specifically add attributes for version, bundle-version
     * 
     * @param importPackageSpecification
     */
    public StandardQuasiParameterised(ImportPackageSpecification importPackageSpecification) {
        this.attributes = normalise(importPackageSpecification.getAttributes());
        this.directives = normalise(importPackageSpecification.getDirectives());
        VersionRange bundleVersionRange = importPackageSpecification.getBundleVersionRange();
        if(bundleVersionRange != null) {
            this.attributes.put(BUNDLE_VERSION_ATTRIBUTE, bundleVersionRange);
        }
        String bundleSymbolicName = importPackageSpecification.getBundleSymbolicName();
        if(StringUtils.hasLength(bundleSymbolicName)) {
            this.attributes.put(BUNDLE_SYMBOLIC_NAME_ATTRIBUTE, bundleSymbolicName);
        }
        VersionRange versionRange = importPackageSpecification.getVersionRange();
        if(versionRange != null) {
            this.attributes.put(VERSION_ATTRIBUTE, versionRange);
        }
    }

    public StandardQuasiParameterised(ExportPackageDescription exportPackageDescription) {
        this.attributes = normalise(exportPackageDescription.getAttributes());
        this.directives = normalise(exportPackageDescription.getDirectives());    
    }

    /**
     * Coerce the given map into an immutable map of String to String.
     */
    private static Map<String, Object> normalise(final Map<String, Object> map) {
        if (map!=null) {
            return new HashMap<String, Object>(map);
        }
        return new HashMap<String, Object>();
    }

    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public Map<String, Object> getDirectives() {
        return this.directives;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("[");
        boolean first = true;
        if (this.directives != null) {
            for (String directiveName : this.directives.keySet()) {
                if (!first) {
                    result.append(", ");
                }
                first = false;
                result.append(directiveName);
                result.append(":=");
                result.append(this.directives.get(directiveName));
            }
        }
        if (this.attributes != null) {
            for (String attributeName : this.attributes.keySet()) {
                if (!first) {
                    result.append(", ");
                }
                first = false;
                result.append(attributeName);
                result.append("=");
                result.append(this.attributes.get(attributeName));
            }
        }
        result.append("]");
        return result.toString();
    }
}
