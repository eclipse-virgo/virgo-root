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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.osgi.service.cm.Configuration;

public class StubConfiguration implements Configuration {

    private Hashtable properties = null;

    private final String pid;

    private final String factoryPid;

    public StubConfiguration() {
        this(null, null);
    }
    
    public StubConfiguration(String pid, String factoryPid) {
        this.pid = pid;
        this.factoryPid = factoryPid;
    }

    public void delete() throws IOException {
        return;
    }

    public String getBundleLocation() {
        throw new UnsupportedOperationException();
    }

    public String getFactoryPid() {
        return factoryPid;
    }

    public String getPid() {
        return pid;
    }

    @SuppressWarnings("unchecked")
    public Dictionary getProperties() {
        if (this.properties == null)
            return null;
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

    @SuppressWarnings("unchecked")
	public void update(Dictionary dictionary) throws IOException {
    	Hashtable<String, String> htable = (Hashtable) dictionary;
    	Set<String> keySet = htable.keySet();
    	
    	Iterator iterator = keySet.iterator();
    	while (iterator.hasNext()) {
			String key = (String) iterator.next();

	    	Set<String> keyChecks = new HashSet<String>();
			keyChecks.addAll(keySet);
			keyChecks.remove(key);
			
			Iterator iterator2 = keyChecks.iterator();
			while (iterator2.hasNext()) {
	    		String keyCheck = (String)iterator2.next();
				if(key.equalsIgnoreCase(keyCheck)){
	    			throw new IllegalArgumentException(key + " is already present or is a case variant.");
	    		}
	    	}
		}
        this.properties = (Hashtable) dictionary;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pid == null) ? 0 : pid.hashCode());
        return result;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StubConfiguration other = (StubConfiguration) obj;
        if (pid == null) {
            if (other.pid != null)
                return false;
        } else if (!pid.equals(other.pid))
            return false;
        return true;
    }
}
