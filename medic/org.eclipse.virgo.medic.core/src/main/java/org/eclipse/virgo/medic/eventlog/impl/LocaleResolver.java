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

package org.eclipse.virgo.medic.eventlog.impl;

import java.util.Locale;

/**
 * A <code>LocaleResolver</code> provides a basic strategy for determining the current {@link Locale}.
 * <strong>Concurrent Semantics</strong><br />
 * Implementations <strong>must</strong> be thread-safe.
 * 
 */
public interface LocaleResolver {

    /**
     * Return the current {@link Locale}. This should never return <code>null</code>.
     * 
     * @return the current <code>Locale</code>. Never <code>null</code>.
     */
    Locale getLocale();
}
