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

package org.eclipse.virgo.shell.internal.converters;

import org.eclipse.virgo.shell.Converter;



/**
 */
final public class StringConverter implements Converter {

    private static final String TYPES = String.class.getName();

    /**
     * Simple getter used when this class is created as a bean and placed in the service registry.
     * 
     * @return String or String[] of the types this converter can convert
     */
    public static String getTypes() {
        return TYPES;
    }

    /** 
     * {@inheritDoc}
     */
    public Object convert(Class<?> desiredType, Object in) throws Exception {
        if(desiredType.equals(String.class)){
            return in.toString();
        }
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public CharSequence format(Object target, int level, Converter escape) throws Exception {
        if(target instanceof String){
            return target.toString();
        }
        return null;
    }

}
