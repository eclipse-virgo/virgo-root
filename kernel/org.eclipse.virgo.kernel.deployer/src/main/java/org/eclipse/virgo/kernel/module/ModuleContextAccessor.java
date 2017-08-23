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

package org.eclipse.virgo.kernel.module;

import org.eclipse.virgo.nano.serviceability.NonNull;
import org.osgi.framework.Bundle;


/**
 * {@link ModuleContextAccessor} is the kernel standard interface to for accessing {@link ModuleContext ModuleContexts}.
 * 
 */
public interface ModuleContextAccessor {

    /**
     * Returns the {@link ModuleContext} associated with the given {@link Bundle} or <code>null</code> if there is no such <code>ModuleContext</code>.
     * 
     * @param bundle the <code>Bundle</code> whose <code>ModuleContext</code> is required
     * @return a <code>ModuleContext</code> or <code>null</code> if there is no <code>ModuleContext</code> associated with the given <code>Bundle</code>
     */
    ModuleContext getModuleContext(@NonNull Bundle bundle);

}
