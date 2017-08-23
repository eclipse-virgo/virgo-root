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

package org.eclipse.virgo.medic.log.impl.config;

import org.osgi.framework.ServiceReference;

class ServiceReferenceUtils {

    static <S> ServiceReference<S> selectServiceReference(ServiceReference<S>[] serviceReferences) {
        ServiceReference<S> highest = null;

        for (ServiceReference<S> serviceReference : serviceReferences) {
            if (highest == null || serviceReference.compareTo(highest) > 0) {
                highest = serviceReference;
            }
        }

        return highest;
    }
}
