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

package org.eclipse.virgo.util.osgi.manifest.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class MapUpdatingList extends ArrayList<String> implements List<String> {

    private static final long serialVersionUID = 4379585330343695190L;

    private final String key;

    private final Map<String, String> map;

    MapUpdatingList(Map<String, String> map, String key) {
        this.map = map;
        this.key = key;
    }

    public boolean add(String string) {
        boolean added = super.add(string);
        updateMap();
        return added;
    }

    public void add(int index, String string) {
        super.add(index, string);
        updateMap();
    }

    public boolean addAll(Collection<? extends String> strings) {
        boolean added = super.addAll(strings);
        updateMap();
        return added;
    }

    public boolean addAll(int index, Collection<? extends String> strings) {
        boolean added = super.addAll(index, strings);
        updateMap();
        return added;
    }

    public void clear() {
        super.clear();
        updateMap();
    }

    public String remove(int index) {
        String removed = super.remove(index);
        updateMap();        
        return removed;
    }

    public boolean remove(Object o) {
        boolean removed = super.remove(o);
        if (removed) {
            updateMap();
        }
        return removed;
    }

    private void updateMap() {
        String value = HeaderUtils.toString(this);

        if (value != null) {
            this.map.put(this.key, HeaderUtils.toString(this));
        } else {
            this.map.remove(this.key);
        }
    }

    public boolean removeAll(Collection<?> c) {
        boolean altered = super.removeAll(c);
        if (altered) {
            updateMap();
        }
        return altered;
    }

    public boolean retainAll(Collection<?> c) {
        boolean altered = super.retainAll(c);
        if (altered) {
            updateMap();
        }
        return altered;
    }

    public String set(int index, String string) {
        String replaced = super.set(index, string);
        updateMap();
        return replaced;
    }
}
