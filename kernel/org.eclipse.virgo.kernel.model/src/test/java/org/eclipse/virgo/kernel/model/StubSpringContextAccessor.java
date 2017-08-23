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

package org.eclipse.virgo.kernel.model;

import org.eclipse.virgo.kernel.model.internal.SpringContextAccessor;
import org.osgi.framework.Bundle;

/**
 * Stub impl
 */
public class StubSpringContextAccessor implements SpringContextAccessor {

    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean isSpringPowered(Bundle bundle) {
        return false;
    }

}
