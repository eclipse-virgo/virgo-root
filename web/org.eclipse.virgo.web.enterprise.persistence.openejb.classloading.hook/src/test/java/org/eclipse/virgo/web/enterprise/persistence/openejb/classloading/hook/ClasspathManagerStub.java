/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.persistence.openejb.classloading.hook;

import java.security.ProtectionDomain;

import org.eclipse.osgi.internal.loader.ModuleClassLoader;
import org.eclipse.osgi.internal.loader.classpath.ClasspathEntry;
import org.eclipse.osgi.internal.loader.classpath.ClasspathManager;
import org.eclipse.osgi.storage.BundleInfo.Generation;

public class ClasspathManagerStub extends ClasspathManager {

    private boolean shouldReturnNull = false;

    public ClasspathManagerStub(Generation data, ModuleClassLoader classloader) {
        super(data, classloader);
    }

    public ClasspathEntry getExternalClassPath(String cp, Generation sourcedata, ProtectionDomain sourcedomain) {
        if (shouldReturnNull) {
            return null;
        }

        return new ClasspathEntry(new BundleFileStub(cp), sourcedomain, sourcedata);
    }

    public void setShouldReturnNull(boolean shouldReturnNull) {
        this.shouldReturnNull = shouldReturnNull;
    }

}
