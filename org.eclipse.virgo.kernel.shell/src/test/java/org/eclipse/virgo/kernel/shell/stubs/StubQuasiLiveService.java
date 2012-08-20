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

package org.eclipse.virgo.kernel.shell.stubs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;


public class StubQuasiLiveService implements QuasiLiveService {

    private Map<String, Object> properties = new HashMap<String,Object>();
    
    private long serviceId;
    
    private QuasiBundle provider;

    public StubQuasiLiveService(long serviceId, QuasiBundle provider) {
        this.serviceId = serviceId;
        this.provider = provider;
    }
    
    public List<QuasiBundle> getConsumers() {
        return new ArrayList<QuasiBundle>();
    }

    public boolean setProperty(String name, Object value) {
        Object oldValue = this.properties.put(name, value);
        return (oldValue!=null);
    }

    public QuasiBundle getProvider() {
        return provider;
    }

    public long getServiceId() {
        return this.serviceId;
    }

    public int compareTo(QuasiLiveService o) {
        return 0;
    }

    public Map<String, Object> getProperties() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.putAll(this.properties);
        return map;
    }
    
}
