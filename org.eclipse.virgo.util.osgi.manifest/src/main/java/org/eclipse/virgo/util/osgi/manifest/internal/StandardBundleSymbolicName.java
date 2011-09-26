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

package org.eclipse.virgo.util.osgi.manifest.internal;

import org.eclipse.virgo.util.osgi.manifest.BundleSymbolicName;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;
import org.osgi.framework.Constants;



/**
 * <strong>Concurrent Semantics</strong><br />
 * Not thread-safe.
 */
class StandardBundleSymbolicName  extends BaseParameterised implements BundleSymbolicName {

    StandardBundleSymbolicName(HeaderParser parser) {
        super(parser);
    }

    @Override
    HeaderDeclaration parse(HeaderParser parser, String parseString) {
        return parser.parseBundleSymbolicName(parseString);
    }

    /** 
     * {@inheritDoc}
     */
    public FragmentAttachment getFragmentAttachment() {        
        String value = this.getDirectives().get(Constants.FRAGMENT_ATTACHMENT_DIRECTIVE);
        if (Constants.FRAGMENT_ATTACHMENT_NEVER.equals(value)) {
            return FragmentAttachment.NEVER;
        } else if (Constants.FRAGMENT_ATTACHMENT_RESOLVETIME.equals(value)) {
            return FragmentAttachment.RESOLVE_TIME;
        } else {
            return FragmentAttachment.ALWAYS;
        }
    }

    /** 
     * {@inheritDoc}
     */
    public String getSymbolicName() {
        return this.name;
    }

    /** 
     * {@inheritDoc}
     */
    public boolean isSingleton() {
        return Boolean.valueOf(this.getDirectives().get(Constants.SINGLETON_DIRECTIVE));
    }

    /** 
     * {@inheritDoc}
     */
    public void setFragmentAttachment(FragmentAttachment fragmentAttachment) {
        switch (fragmentAttachment) {
            case NEVER:
                this.getDirectives().put(Constants.FRAGMENT_ATTACHMENT_DIRECTIVE, Constants.FRAGMENT_ATTACHMENT_NEVER);
                break;
            case RESOLVE_TIME:
                this.getDirectives().put(Constants.FRAGMENT_ATTACHMENT_DIRECTIVE, Constants.FRAGMENT_ATTACHMENT_RESOLVETIME);
                break;
            default:
                this.getDirectives().remove(Constants.FRAGMENT_ATTACHMENT_DIRECTIVE);
        }        
    }

    /** 
     * {@inheritDoc}
     */
    public void setSingleton(boolean singleton) {
        this.getDirectives().put(Constants.SINGLETON_DIRECTIVE, Boolean.toString(singleton));         
    }

    /** 
     * {@inheritDoc}
     */
    public void setSymbolicName(String symbolicName) {
        this.name = symbolicName;
    }
}
