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

package org.eclipse.virgo.medic.log.impl;

import org.osgi.framework.Bundle;

public interface CallingBundleResolver {

    /**
     * Returns the {@link Bundle} from which the current logging call originated.
     * 
     * @return the <code>Bundle</code> that made the logging call
     */
    Bundle getCallingBundle();
}
