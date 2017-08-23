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

package org.eclipse.virgo.util.math;

/**
 * This class is thread safe.
 * 
 * @param <T> type of Comparable element
 */

public final class Range<T extends Comparable<? super T>> {

	public static final String FLOOR_INCLUSIVE_DELIMITER = "[";

	public static final String CEILING_INCLUSIVE_DELIMITER = "]";

	public static final String FLOOR_EXCLUSIVE_DELIMITER = "(";

	public static final String CEILING_EXCLUSIVE_DELIMITER = ")";

	public static final String SEPARATOR = ",";

	private final T floor, ceiling;

	private final boolean floorInc, ceilingInc;

	public Range(T floor, boolean floorInc, T ceiling, boolean ceilingInc) {
		this.floor = floor;
		this.floorInc = floorInc;
		this.ceiling = ceiling;
		this.ceilingInc = ceilingInc;
	}

	public boolean contains(T t) {
		int minCheck = this.floorInc ? 0 : 1;
		int maxCheck = this.ceilingInc ? 0 : -1;
		return t.compareTo(this.floor) >= minCheck
				&& t.compareTo(this.ceiling) <= maxCheck;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(this.floorInc ? FLOOR_INCLUSIVE_DELIMITER
				: FLOOR_EXCLUSIVE_DELIMITER);
		result.append(this.floor.toString());
		result.append(SEPARATOR);
		result.append(this.ceiling.toString());
		result.append(this.ceilingInc ? CEILING_INCLUSIVE_DELIMITER
				: CEILING_EXCLUSIVE_DELIMITER);
		return result.toString();
	}
}
