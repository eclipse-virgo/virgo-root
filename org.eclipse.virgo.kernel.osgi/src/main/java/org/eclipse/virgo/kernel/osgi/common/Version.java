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

package org.eclipse.virgo.kernel.osgi.common;

import java.util.Arrays;
import java.util.List;

import org.eclipse.virgo.util.math.Range;

/**
 * This class is thread safe.
 * 
 */
public final class Version implements Comparable<Version> {

    private static final int DEFAULT_VERSION_COMPONENT = 0;

    private static final String MINIMUM_VERSION_STRING = "0";

    public static final Version MINIMUM_VERSION = new Version(MINIMUM_VERSION_STRING);

    public static final Version MAXIMUM_VERSION = new Version(Arrays.asList(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));

    private final org.osgi.framework.Version version;

    public Version(String v) {
        this.version = org.osgi.framework.Version.parseVersion(v);
    }

    public Version(org.osgi.framework.Version version) {
        this.version = version;
    }

    public Version(List<Integer> comp) {
        if (comp.size() > 3) {
            throw new NumberFormatException("Versions may have at most three numeric components");
        }
        int major = comp.size() >= 1 ? comp.get(0) : DEFAULT_VERSION_COMPONENT;
        int minor = comp.size() >= 2 ? comp.get(1) : DEFAULT_VERSION_COMPONENT;
        int micro = comp.size() == 3 ? comp.get(2) : DEFAULT_VERSION_COMPONENT;
        this.version = new org.osgi.framework.Version(major, minor, micro);
    }

    /**
     * @see Integer#compareTo
     * 
     * @param v2
     * @return -1 if this < v2, 0 if this equals v2, 1 if this > v2
     */
    public int compareTo(Version v2) {
        return this.version.compareTo(v2.version);
    }

    /**
     * Convert a version range string following OSGi conventions into a Range<Version>.
     * 
     * OSGi conventions are that ranges are specified using standard mathematical interval notation except for the
     * special case of a version, v, on its own which denotes the range [v, MAXIMUM_VERSION).
     * 
     * @param stringRange a version range string
     * @return the Range<String> corresponding to the given version range string
     */
    public static Range<Version> stringToVersionRange(String stringRange) {
        if (stringRange == null) {
            return stringToVersionRange(MINIMUM_VERSION_STRING);
        }
        boolean floorInc = stringRange.startsWith(Range.FLOOR_INCLUSIVE_DELIMITER);
        boolean floorExc = stringRange.startsWith(Range.FLOOR_EXCLUSIVE_DELIMITER);
        boolean ceilingInc = stringRange.endsWith(Range.CEILING_INCLUSIVE_DELIMITER);
        boolean ceilingExc = stringRange.endsWith(Range.CEILING_EXCLUSIVE_DELIMITER);
        int separatorIndex = stringRange.indexOf(Range.SEPARATOR);
        boolean delimitedRange = (floorInc || floorExc) && (ceilingInc || ceilingExc) && separatorIndex != -1;

        if (delimitedRange) {
            String floor = stringRange.substring(1, separatorIndex);
            String ceiling = stringRange.substring(separatorIndex + 1, stringRange.length() - 1);
            return new Range<Version>(new Version(floor), floorInc, new Version(ceiling), ceilingInc);
        } else {
            return new Range<Version>(new Version(stringRange), true, MAXIMUM_VERSION, false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public int hashCode() {
        return this.version.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Version other = (Version) obj;
        return this.version.equals(other.version);
    }

    /**
     * {@inheritDoc}
     */
    @Override public String toString() {
        String result = this.version.toString();
        while (result.endsWith(".0")) {
            result = result.substring(0, result.length() - 2);
        }
        return result;
    }
}
