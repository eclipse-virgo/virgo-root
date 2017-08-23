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
package org.eclipse.virgo.kernel.model.internal;

import org.osgi.framework.Bundle;

/**
 * SpringContextAccessor 
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * TODO Document concurrent semantics of SpringContextAccessor
 */
public interface SpringContextAccessor {

    /**
     * Inspects the service registry to find out if the {@link Bundle} registers 
     * a service for a Spring ApplicationContext, must be able to handle multiple 
     * instances of the Spring framework running in the OSGi framework.
     * 
     * @param bundle to inspect
     * @return true if Spring found
     */
    public boolean isSpringPowered(Bundle bundle);

}