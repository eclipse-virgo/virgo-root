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

package org.eclipse.virgo.shell.internal.help;

import java.util.List;


/**
 * Helper interface that retrieves summary and detailed help for a given shell class, if it exists.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Implementations must be thread-safe.
 *
 */
public interface HelpAccessor {

    /**
     * Get the summary string (for use in a list) for the {@link Class} <code>clazz</code> obtained
     * from a help resource file in the bundle in which the class belongs.
     * <br/>
     * <code>null</code> is returned if the class is not part of a bundle, or if no help resource is found there.
     * @param clazz for which help is retrieved
     * @return summary string for {@link Class} <code>clazz</code> - typically one line.
     */
    String getSummaryHelp(Class<?> clazz);
    
    /**
     * Get the detailed string (for use in a single description) for the {@link Class} <code>clazz</code> obtained
     * from a help resource file in the bundle in which the class belongs.
     * <br/>
     * <code>null</code> is returned if the class is not part of a bundle, or if no help resource is found there.
     * @param clazz for which help is retrieved
     * @return summary string for {@link Class} <code>clazz</code> - typically multi-line.
     */
    List<String> getDetailedHelp(Class<?> clazz);
    
}
