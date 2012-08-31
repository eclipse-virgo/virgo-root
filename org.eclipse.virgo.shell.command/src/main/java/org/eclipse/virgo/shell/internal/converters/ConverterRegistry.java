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

package org.eclipse.virgo.shell.internal.converters;

import org.eclipse.virgo.shell.Converter;

/**
 * A <code>ConverterRegistry</code> provides access to all of the currently available {@link Converter Converters}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be thread-safe.
 * 
 */
public interface ConverterRegistry {

    /**
     * Returns the converter for the given <code>clazz</code>
     * 
     * @param clazz The Class for which a <code>Converter</code> is required
     * @return The <code>Converter</code> for the Class, or <code>null</code> if no <code>Converter</code> is available.
     */
    Converter getConverter(Class<?> clazz);

}
