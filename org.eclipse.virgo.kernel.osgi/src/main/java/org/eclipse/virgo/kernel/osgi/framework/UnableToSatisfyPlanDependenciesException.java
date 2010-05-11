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

package org.eclipse.virgo.kernel.osgi.framework;

import org.osgi.framework.Version;

/**
 * Signals an error resolving the dependencies of a plan during installation.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public class UnableToSatisfyPlanDependenciesException extends UnableToSatisfyDependenciesException {

    private static final long serialVersionUID = 903287709449316407L;

    private static final String PLAN_ENTITY = "plan";

    public UnableToSatisfyPlanDependenciesException(String symbolicName, Version version, String failureDescription) {
        super(PLAN_ENTITY, symbolicName, version, failureDescription);
    }

}
