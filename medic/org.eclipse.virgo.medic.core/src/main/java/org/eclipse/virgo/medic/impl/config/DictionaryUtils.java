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

package org.eclipse.virgo.medic.impl.config;

import java.util.Dictionary;
import java.util.Enumeration;

final class DictionaryUtils {

    static void merge(Dictionary<Object, Object> primary, Dictionary<Object, Object> secondary) {
        Enumeration<?> keys = secondary.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (primary.get(key) == null) {
                primary.put(key, secondary.get(key));
            }
        }
    }
    
    static <S, T> void mergeGeneral(Dictionary<S, T> primary, Dictionary<S, T> secondary) {
        Enumeration<S> keys = secondary.keys();
        while (keys.hasMoreElements()) {
            S key = keys.nextElement();
            if (primary.get(key) == null) {
                primary.put(key, secondary.get(key));
            }
        }
    }
}
