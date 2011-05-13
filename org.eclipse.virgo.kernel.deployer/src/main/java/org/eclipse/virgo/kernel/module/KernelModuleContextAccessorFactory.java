/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.module;

import org.eclipse.virgo.kernel.module.internal.KernelModuleContextAccessor;

/**
 * {@link KernelModuleContextAccessorFactory} is used to construct {@link ModuleContextAccessor} instances for the
 * kernel.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
public class KernelModuleContextAccessorFactory {
    
    public static ModuleContextAccessor create() {
        return new KernelModuleContextAccessor();
    }

}
