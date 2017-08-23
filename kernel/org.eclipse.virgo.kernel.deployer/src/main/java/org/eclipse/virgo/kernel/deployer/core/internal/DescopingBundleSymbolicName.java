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

import java.util.Map;

import org.eclipse.virgo.util.osgi.manifest.BundleSymbolicName;

/**
 * {@link DescopingBundleSymbolicName} is a wrapper of a {@link BundleSymbolicName} that reverses the effects of
 * scoping.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class DescopingBundleSymbolicName implements BundleSymbolicName {

    public static final String SCOPE_SEPARATOR = "-";

    private final BundleSymbolicName wrappedBundleSymbolicName;

    private final String moduleScope;

    DescopingBundleSymbolicName(BundleSymbolicName bundleSymbolicName, String moduleScope) {
        this.wrappedBundleSymbolicName = bundleSymbolicName;
        this.moduleScope = moduleScope;
    }

    /**
     * {@inheritDoc}
     */
    public FragmentAttachment getFragmentAttachment() {
        return this.wrappedBundleSymbolicName.getFragmentAttachment();
    }

    /**
     * {@inheritDoc}
     */
    public String getSymbolicName() {
        return getUnscopedSymbolicName();
    }

    private String getUnscopedSymbolicName() {
        String symbolicName = null;
        if (this.wrappedBundleSymbolicName != null) {
            symbolicName = this.wrappedBundleSymbolicName.getSymbolicName();
            if (this.moduleScope != null) {
                String scopeName = this.moduleScope + SCOPE_SEPARATOR;
                if (symbolicName.startsWith(scopeName)) {
                    symbolicName = symbolicName.substring(scopeName.length());
                }
            }
        }
        return symbolicName;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSingleton() {
        return this.wrappedBundleSymbolicName.isSingleton();
    }

    /**
     * {@inheritDoc}
     */
    public void setFragmentAttachment(FragmentAttachment fragmentAttachment) {
        this.wrappedBundleSymbolicName.setFragmentAttachment(fragmentAttachment);
    }

    /**
     * {@inheritDoc}
     */
    public void setSingleton(boolean singleton) {
        this.wrappedBundleSymbolicName.setSingleton(singleton);
    }

    /**
     * {@inheritDoc}
     */
    public void setSymbolicName(String symbolicName) {
        this.wrappedBundleSymbolicName.setSymbolicName(symbolicName);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getAttributes() {
        return this.wrappedBundleSymbolicName.getAttributes();
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getDirectives() {
        return this.wrappedBundleSymbolicName.getDirectives();
    }

    /**
     * {@inheritDoc}
     */
    public void resetFromParseString(String string) {
       this.wrappedBundleSymbolicName.resetFromParseString(string);
    }

    /**
     * {@inheritDoc}
     */
    public String toParseString() {
        return this.wrappedBundleSymbolicName.toParseString();
    }

}
