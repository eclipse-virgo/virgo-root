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

package org.eclipse.virgo.apps.admin.core.stubs;

import org.osgi.framework.Bundle;

import org.eclipse.virgo.kernel.services.work.WorkArea;
import org.eclipse.virgo.util.io.PathReference;


/**
 */
public class StubWorkArea implements WorkArea {

    /** 
     * {@inheritDoc}
     */
    public Bundle getOwner() {
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public PathReference getWorkDirectory() {
        return new PathReference("target");
    }

}
