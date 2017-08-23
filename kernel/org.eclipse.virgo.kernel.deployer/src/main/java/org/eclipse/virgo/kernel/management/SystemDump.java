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

package org.eclipse.virgo.kernel.management;

import javax.management.MXBean;

/**
 * <p>
 * Defines the operations available on the exported control for generation dumps on demand
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations of this class should be thread safe
 *
 */
@MXBean
public interface SystemDump {

    /**
     * Generate a system dump now, must not require or wait for any exception to be thrown.
     */
    public void generateDump();

}
