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

package org.eclipse.virgo.kernel.userregion.internal.quasi;

import java.io.Serializable;
import java.util.Comparator;

import org.eclipse.osgi.service.resolver.BundleDescription;

class BundleDescriptionComparator implements Comparator<BundleDescription>, Serializable {
    
    private static final long serialVersionUID = 1360592459106362605L;

    public int compare(BundleDescription bd1, BundleDescription bd2) {
        int comparison = bd1.getSymbolicName().compareTo(bd2.getSymbolicName());
        
        if (comparison == 0) {
            comparison = bd1.getVersion().compareTo(bd2.getVersion());
        }
        
        return comparison;
    }
}
