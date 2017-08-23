/*******************************************************************************
 * Copyright (c) 2013 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.persistence.openejb.classloading.hook;

import java.util.Dictionary;

import org.eclipse.osgi.baseadaptor.BaseAdaptor;
import org.eclipse.osgi.baseadaptor.BaseData;
import org.osgi.framework.BundleException;

public class BaseDataStub extends BaseData {
	private boolean shouldThrowException = false;

	public BaseDataStub(long id, BaseAdaptor adaptor) {
		super(id, adaptor);
	}

	public Dictionary<String, String> getManifest() throws BundleException {
		if (shouldThrowException) {
			throw new BundleException("BaseData test exception");
		}
		
		return manifest;
	}
	
	public void setManifest(Dictionary<String, String> manifest) {
		this.manifest = manifest;
	}
	
	public void setShouldThrowException(boolean shouldThrowException) {
		this.shouldThrowException = shouldThrowException;
	}
}
