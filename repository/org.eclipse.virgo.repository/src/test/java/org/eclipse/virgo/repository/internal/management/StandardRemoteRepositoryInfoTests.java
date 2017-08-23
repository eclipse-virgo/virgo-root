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

package org.eclipse.virgo.repository.internal.management;

import org.eclipse.virgo.repository.internal.ArtifactDescriptorDepository;
import org.eclipse.virgo.repository.internal.management.AbstractRepositoryInfo;
import org.eclipse.virgo.repository.internal.management.StandardRemoteRepositoryInfo;

public class StandardRemoteRepositoryInfoTests extends AbstractRepositoryInfoTests {
    /** 
     * {@inheritDoc}
     */
    @Override
    protected AbstractRepositoryInfo getRepositoryInfo(ArtifactDescriptorDepository depository) {
        return new StandardRemoteRepositoryInfo("unittest", depository);
    }
}
