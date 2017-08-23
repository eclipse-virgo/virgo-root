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

package refresh.importer;

import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.Bundle;

import org.eclipse.virgo.kernel.deployer.test.LoadableClasses;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFramework;

public class Importer implements LoadableClasses {

    private final Set<String> loadableClasses = new HashSet<String>();

    public Importer(OsgiFramework osgi) {
        System.out.println("New Importer bean");
        checkLoadClass("refresh.exporter.b1.B11");
        checkLoadClass("refresh.exporter.b1.B12");
        checkLoadClass("refresh.exporter.b2.B21");
        Bundle[] bundles = osgi.getBundleContext().getBundles();
        for (Bundle bundle : bundles) {
            if (bundle.getSymbolicName().equals("RefreshTest-1-RefreshExporter")) {
                System.out.println(bundle.getHeaders().get("bundle-version"));
            }
        }
    }

    private void checkLoadClass(String className) {
        try {
            this.getClass().getClassLoader().loadClass(className);
            System.out.println(className + " is loadable");
            loadableClasses.add(className);
        } catch (ClassNotFoundException e) {
            System.out.println(className + " is not loadable");
        }
    }

    public Set<String> getLoadableClasses() {
        return this.loadableClasses;
    }

}
