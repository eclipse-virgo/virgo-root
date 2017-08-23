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

package org.eclipse.virgo.util.osgi.manifest;


/**
 * Represents a bundle's <code>Bundle-SymbolicName</code> header.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * May not be thread safe.
 */
public interface BundleSymbolicName extends Parameterised {

    /**
     * An enumeration of the legal values for a bundle symbolic name's <code>fragment-attachment</code> directive.
     * <p />
     */
    public enum FragmentAttachment {
        /**
         * Fragments can attach at any time while the host is resolved or during the process of resolving.
         */
        ALWAYS,
        /**
         * Fragments may not attach to the bundle.
         */
        NEVER,
        /**
         * Fragments must only be attached while the bundle is resolving.
         */
        RESOLVE_TIME;
    }

    /**
     * Returns the header's symbolic name, or <code>null</code> if no symbolic name is specified.
     * 
     * @return the symbolic name
     */
    String getSymbolicName();

    /**
     * Sets the header's symbolic name
     * 
     * @param symbolicName the symbolic name
     */
    void setSymbolicName(String symbolicName);

    /**
     * Returns the value of the <code>singleton</code> directive. Returns the default value of <code>false</code> if no
     * <code>singleton</code> directive is specified.
     * 
     * @return the singleton directive
     */
    boolean isSingleton();

    /**
     * Sets the value of the <code>singleton</code> directive.
     * @param singleton the singleton directive's value
     */
    void setSingleton(boolean singleton);

    /**
     * Returns value of the <code>fragment-attachment</code> directive. Returns the default value of <code>ALWAYS</code>
     * if no <code>fragment-attachment</code> directive is specified.
     * 
     * @return the <code>fragment-attachment</code> directive
     */
    FragmentAttachment getFragmentAttachment();

    /**
     * Sets the value of the <code>fragment-attachment</code> directive.
     * 
     * @param fragmentAttachment the fragment-attachment directive's value
     */
    void setFragmentAttachment(FragmentAttachment fragmentAttachment);
}
