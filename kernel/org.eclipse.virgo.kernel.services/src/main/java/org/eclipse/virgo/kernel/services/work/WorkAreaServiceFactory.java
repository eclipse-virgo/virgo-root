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

import java.io.File;

import org.eclipse.virgo.nano.serviceability.NonNull;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;


/**
 * {@link ServiceFactory} that creates a {@link WorkArea} for a given {@link Bundle}. Bundles need only to import a
 * <code>WorkArea</code> service in their module context file to get access to the correct work area location.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public final class WorkAreaServiceFactory implements ServiceFactory<WorkArea> {

    private final File workDirectory;

    public WorkAreaServiceFactory(@NonNull File workDirectory) {
        this.workDirectory = workDirectory;
    }

    public WorkArea getService(Bundle bundle, ServiceRegistration<WorkArea> registration) {
        return new StandardWorkArea(this.workDirectory, bundle);
    }

    public void ungetService(Bundle bundle, ServiceRegistration<WorkArea> registration, WorkArea service) {
    }
}
