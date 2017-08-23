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

package org.eclipse.virgo.test.stubs.service.event.internal;

import java.util.Arrays;

import org.osgi.service.event.Event;


/**
 * Utility methods for working with {@link Event Events}.
 * 
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public final class EventUtils {
    
    /**
     * Returns <code>true</code> if the supplied Events are equal, <code>false</code>
     * if they are not. For two Events to be equals, their topics must be 
     * {@link String#equals(Object)} equal, and their properties must be equal.
     * 
     * <p/>
     * 
     * Note: when considering property equality, one-dimensional arrays are considered
     * to be equal if their contents are equal. Multi-dimensional arrays are not
     * currently considered and will always be reported as unequal.
     * 
     * @param event one <code>Event</code> to be tested for equality
     * @param candidate the other <code>Event</code> to be tested for equality
     * 
     * @return <code>true</code> if the events are equal, otherwise <code>false</code>.
     */
    public static boolean eventsAreEqual(Event event, Event candidate) {
        if (candidate.getTopic().equals(event.getTopic())) {
            String[] propertyNames = event.getPropertyNames();
            String[] candidatePropertyNames = candidate.getPropertyNames();
            
            if (Arrays.equals(propertyNames, candidatePropertyNames)) {
                for (String propertyName : propertyNames) {
                    Object value = event.getProperty(propertyName);
                    Object candidateValue = candidate.getProperty(propertyName);
                    
                    if (value.getClass().isArray()) {
                        if (!arraysAreEqual(value, candidateValue)) {
                            return false;
                        }        
                    } else if (!value.equals(candidateValue)) {
                        return false;
                    }
                }
                return true;
            }
        }
        
        return false;
    }
    
    static boolean arraysAreEqual(Object value, Object candidateValue) {
        boolean arraysAreEqual = false;
        
        Class<?> componentType = value.getClass().getComponentType();
        if (!componentType.isPrimitive()) {
            arraysAreEqual = Arrays.equals((Object[]) value, (Object[]) candidateValue);                                    
        } else if (componentType.equals(boolean.class)) {
            arraysAreEqual = Arrays.equals((boolean[]) value, (boolean[]) candidateValue);
        } else if (componentType.equals(byte.class)) {
            arraysAreEqual = Arrays.equals((byte[]) value, (byte[]) candidateValue);
        } else if (componentType.equals(char.class)) {
            arraysAreEqual = Arrays.equals((char[]) value, (char[]) candidateValue);
        } else if (componentType.equals(double.class)) {
            arraysAreEqual = Arrays.equals((double[]) value, (double[]) candidateValue);
        } else if (componentType.equals(float.class)) {
            arraysAreEqual = Arrays.equals((float[]) value, (float[]) candidateValue);
        } else if (componentType.equals(int.class)) {
            arraysAreEqual = Arrays.equals((int[]) value, (int[]) candidateValue);
        } else if (componentType.equals(long.class)) {
            arraysAreEqual = Arrays.equals((long[]) value, (long[]) candidateValue);
        } else {
            arraysAreEqual = Arrays.equals((short[]) value, (short[]) candidateValue);
        }
        
        return arraysAreEqual;
    }
}
