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

package org.eclipse.virgo.util.osgi.manifest.parse;

/**
 * Signals an exception during parsing of an OSGi bundle manifest.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Threadsafe.
 * 
 */
public class BundleManifestParseException extends RuntimeException {

    private static final long serialVersionUID = -1858207019089158397L;

    public BundleManifestParseException(String message) {
        super(message);
    }

    public BundleManifestParseException(String message, Throwable cause) {
        super(message, cause);
    }

}
