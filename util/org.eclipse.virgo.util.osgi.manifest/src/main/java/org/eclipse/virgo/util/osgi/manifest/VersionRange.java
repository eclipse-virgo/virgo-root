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

package org.eclipse.virgo.util.osgi.manifest;

import org.osgi.framework.Version;

/**
 * Parses the <code>String</code> specification a range of {@link Version Versions} as defined in 3.2.5 of the OSGi
 * Service Server Core Specification.
 * <p/>
 * 
 * The <code>VersionRange</code> can be queried to see if it includes a particular {@link Version} using
 * {@link #includes(Version)}.
 * <p/>
 * 
 * Distinct representations of an empty range are regarded as equal.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Implementation is immutable.
 * 
 */
public final class VersionRange {

    private static final Version ZERO_VERSION = Version.emptyVersion;

    private static final char INCLUSIVE_LOWER = '[';

    private static final char INCLUSIVE_UPPER = ']';

    private static final char EXCLUSIVE_LOWER = '(';

    private static final char EXCLUSIVE_UPPER = ')';

    public static final VersionRange NATURAL_NUMBER_RANGE = new VersionRange(null);

    private final Version floor;

    // The range is unbounded if and only if ceiling == null.
    private final Version ceiling;

    private final boolean floorInclusive;

    private final boolean ceilingInclusive;

    /**
     * Creates a <code>VersionRange</code> for the provided specification.
     * 
     * @param versionRange the <code>VersionRange</code> specification.
     */
    public VersionRange(String versionRange) {
        if (versionRange == null || versionRange.length() == 0) {
            this.floor = ZERO_VERSION;
            this.ceiling = null;
            this.floorInclusive = true;
            this.ceilingInclusive = false;
            return;
        }

        char first = versionRange.charAt(0);
        if (first == INCLUSIVE_LOWER || first == EXCLUSIVE_LOWER) {
            char last = versionRange.charAt(versionRange.length() - 1);
            if (last == INCLUSIVE_UPPER || last == EXCLUSIVE_UPPER) {
                int comma = versionRange.indexOf(',');
                if (comma < 0) {
                    throw new IllegalArgumentException("Version range '" + versionRange + "' is invalid.");
                }
                this.floor = Version.parseVersion(versionRange.substring(1, comma).trim());
                this.floorInclusive = first == INCLUSIVE_LOWER;
                this.ceiling = Version.parseVersion(versionRange.substring(comma + 1, versionRange.length() - 1).trim());
                this.ceilingInclusive = last == INCLUSIVE_UPPER;
            } else {
                throw new IllegalArgumentException("Version range '" + versionRange + "' is invalid.");
            }
        } else {
            this.floor = Version.parseVersion(versionRange);
            this.floorInclusive = true;
            this.ceiling = null;
            this.ceilingInclusive = false;
        }
    }

    private VersionRange(boolean floorInclusive, Version floor, Version ceiling, boolean ceilingInclusive) {
        this.floorInclusive = floorInclusive;
        this.floor = floor;
        this.ceiling = ceiling;
        this.ceilingInclusive = ceilingInclusive;
    }

    /**
     * Creates a <code>VersionRange</code> encompassing all the natural numbers: <code>[0.0.0, infinity)</code>.
     * 
     * @return a <code>VersionRange</code> encompassing all the natural numbers.
     */
    public static VersionRange naturalNumberRange() {
        return NATURAL_NUMBER_RANGE;
    }

    /**
     * Creates a <code>VersionRange</code> that encompasses the supplied version, and only the supplied version:
     * <code>[version, version]</code>.
     * 
     * @param version The version for which an exact range is required.
     * @return The exact range.
     */
    public static VersionRange createExactRange(Version version) {
        return new VersionRange(true, version, version, true);
    }

    /**
     * Gets the floor of this <code>VersionRange</code>.
     * 
     * @return floor of this <code>VersionRange</code>.
     */
    public Version getFloor() {
        return this.floor;
    }

    /**
     * Gets the ceiling of this <code>VersionRange</code>. The returned <code>Version</code> is <code>null</code> if and
     * only if this <code>VersionRange</code> is unbounded.
     * 
     * @return ceiling of this <code>VersionRange</code> or <code>null</code> if the range is unbounded.
     */
    public Version getCeiling() {
        return this.ceiling;
    }

    /**
     * Indicates whether or not the floor of this <code>VersionRange</code> is inclusive.
     * 
     * @return <code>true</code> if the floor is inclusive; otherwise <code>false</code>.
     */
    public boolean isFloorInclusive() {
        return this.floorInclusive;
    }

    /**
     * Indicates whether or not the ceiling of this <code>VersionRange</code> is inclusive.
     * 
     * @return <code>true</code> if the ceiling is inclusive; otherwise <code>false</code>.
     */
    public boolean isCeilingInclusive() {
        return this.ceilingInclusive;
    }

    /**
     * Queries whether this <code>VersionRange</code> includes the supplied {@link Version}.
     * 
     * @param version the <code>Version</code> to check against.
     * @return <code>true</code> if the <code>Version</code> is included in this <code>VersionRange</code>; otherwise
     *         <code>false</code>.
     */
    public boolean includes(Version version) {
        int minCheck = this.floorInclusive ? 0 : 1;
        int maxCheck = this.ceilingInclusive ? 0 : -1;
        if (this.floor == null) {
            throw new RuntimeException("ff");
        }
        return version.compareTo(this.floor) >= minCheck && (ceiling == null || version.compareTo(this.ceiling) <= maxCheck);
    }

    /**
     * Queries whether this <code>VersionRange</code> is an exact range containing a single version.
     * 
     * @return <code>true</code> if and only if this code>VersionRange</code> is exact
     */
    public boolean isExact() {
        return isCeilingInclusive() && isFloorInclusive() && getFloor().equals(getCeiling());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof VersionRange)) {
            return false;
        }
        VersionRange that = (VersionRange) object;
        return (this.isEmpty() && that.isEmpty())
            || (this.floorInclusive == that.floorInclusive && this.ceilingInclusive == that.ceilingInclusive && this.floor.equals(that.floor) && ((this.ceiling == null && that.ceiling == null) || (this.ceiling != null && this.ceiling.equals(that.ceiling))));
    }

    public boolean isEmpty() {
        if (this.ceiling == null) {
            return false;
        }
        int limitComparison = this.ceiling.compareTo(this.floor);
        if (limitComparison == 0) {
            return !(this.floorInclusive && this.ceilingInclusive);
        } else if (limitComparison < 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = 17;
        if (!isEmpty()) {
            result = 37 * result + this.floor.hashCode();
            result = 37 * result + (this.ceiling == null ? 0 : this.ceiling.hashCode());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new StringBuilder().append(this.floorInclusive ? INCLUSIVE_LOWER : EXCLUSIVE_LOWER).append(this.floor).append(", ").append(
            this.ceiling == null ? "oo" : this.ceiling).append(this.ceilingInclusive ? INCLUSIVE_UPPER : EXCLUSIVE_UPPER).toString();
    }

    /**
     * Creates a <code>String</code> representation of this <code>VersionRange</code> that can be re-parsed.
     * 
     * @return string representation of version range
     */
    public String toParseString() {
        if (this.ceiling == null && this.floorInclusive) {
            return this.floor.toString();
        } else {
            return new StringBuilder().append(this.floorInclusive ? INCLUSIVE_LOWER : EXCLUSIVE_LOWER).append(this.floor).append(", ").append(
                this.ceiling).append(this.ceilingInclusive ? INCLUSIVE_UPPER : EXCLUSIVE_UPPER).toString();
        }
    }

    /**
     * Returns a <code>VersionRange</code> that is the intersection of the two supplied <code>VersionRanges</code>.
     * 
     * @param rangeOne The first <code>VersionRange</code> for the intersection
     * @param rangeTwo The second <code>VersionRange</code> for the intersection
     * @return The intersection of the two <code>VersionRanges</code>
     */
    public static VersionRange intersection(VersionRange rangeOne, VersionRange rangeTwo) {

        Version floor;
        boolean floorInclusive;

        Version ceiling;
        boolean ceilingInclusive;

        int floorComparison = rangeOne.floor.compareTo(rangeTwo.floor);
        if (floorComparison < 0) {
            floor = rangeTwo.floor;
            floorInclusive = rangeTwo.floorInclusive;
        } else if (floorComparison > 0) {
            floor = rangeOne.floor;
            floorInclusive = rangeOne.floorInclusive;
        } else {
            floor = rangeOne.floor;
            floorInclusive = rangeOne.floorInclusive && rangeTwo.floorInclusive;
        }

        if (rangeOne.ceiling == null) {
            if (rangeTwo.ceiling == null) {
                ceiling = null;
                ceilingInclusive = false;
            } else {
                ceiling = rangeTwo.ceiling;
                ceilingInclusive = rangeTwo.ceilingInclusive;
            }
        } else if (rangeTwo.ceiling == null) {
            ceiling = rangeOne.ceiling;
            ceilingInclusive = rangeOne.ceilingInclusive;
        } else {
            int ceilingComparison = rangeOne.ceiling.compareTo(rangeTwo.ceiling);
            if (ceilingComparison > 0) {
                ceiling = rangeTwo.ceiling;
                ceilingInclusive = rangeTwo.ceilingInclusive;
            } else if (ceilingComparison < 0) {
                ceiling = rangeOne.ceiling;
                ceilingInclusive = rangeOne.ceilingInclusive;
            } else {
                ceiling = rangeOne.ceiling;
                ceilingInclusive = rangeOne.ceilingInclusive && rangeTwo.ceilingInclusive;
            }
        }

        return new VersionRange(floorInclusive, floor, ceiling, ceilingInclusive);
    }
}
