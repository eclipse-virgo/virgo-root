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

package org.eclipse.virgo.kernel.services.work;

import org.osgi.framework.Bundle;

import org.eclipse.virgo.util.io.PathReference;

/**
 * Controls access to the work area.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations <strong>must</strong> be threadsafe.
 * 
 */
public interface WorkArea {

    /**
     * Gets the {@link Bundle} that owns this section of the work area.
     * @return the owning <code>Bundle</code>.
     */
    Bundle getOwner();
    
    /**
     * Gets the work directory for this work area.
     * 
     * @return a {@link PathReference} to the work directory.
     */
    PathReference getWorkDirectory();
}
