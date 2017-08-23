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

package org.eclipse.virgo.repository.internal.remote;

import java.io.File;

final class DescriptorStore {
	
	private final String etag;
	
	private final File location;
	
	DescriptorStore(String etag, File location) {
		if (location == null) {
			throw new IllegalArgumentException("DescriptorStore location cannot be null");
		}
		this.etag = etag;
		this.location = location;
	}

	String getEtag() {
		return etag;
	}

	File getLocation() {
		return location;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((etag == null) ? 0 : etag.hashCode());
		result = prime * result + location.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		DescriptorStore other = (DescriptorStore) obj;
		
		// Descriptor stores with null etags are never equal
		if (etag == null) {
			return false;			
		} else if (!etag.equals(other.etag))
			return false;
		
		if (!location.equals(other.location))
			return false;
		
		return true;
	}
}
