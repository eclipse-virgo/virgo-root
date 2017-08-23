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
 * An enumeration of the legal values for the <code>resolution</code> directive
 * of an <code>Import-Package</code>, <code>Import-Bundle</code>, or <code>Import-Library</code>
 * header entry.
 * <p />
 */
public enum Resolution {
    /**
     * The import is mandatory.
     */
    MANDATORY,
    /**
     * The import is optional.
     */
    OPTIONAL
}
