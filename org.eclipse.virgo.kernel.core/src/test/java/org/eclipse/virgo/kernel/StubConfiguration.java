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

package org.eclipse.virgo.kernel;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.cm.Configuration;

public class StubConfiguration implements Configuration {

    private Hashtable properties = new Hashtable();

    public void delete() throws IOException {
        throw new UnsupportedOperationException();
    }

    public String getBundleLocation() {
        throw new UnsupportedOperationException();
    }

    public String getFactoryPid() {
        throw new UnsupportedOperationException();
    }

    public String getPid() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public Dictionary getProperties() {
        Hashtable propertiesCopy = new Hashtable();
        propertiesCopy.putAll(this.properties);
        return propertiesCopy;
    }

    public void setBundleLocation(String arg0) {
        throw new UnsupportedOperationException();
    }

    public void update() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void update(Dictionary dictionary) throws IOException {
        this.properties = (Hashtable)dictionary;
    }
}
