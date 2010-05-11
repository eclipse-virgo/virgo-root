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

package eg;

import org.osgi.framework.*;
import org.eclipse.osgi.framework.adaptor.BundleClassLoader;

public class Activator implements BundleActivator {
	
	public void start(BundleContext ctx) throws BundleException {
		assertTccl("start", ctx.getBundle());
	}
	
	public void stop(BundleContext ctx) throws BundleException {
		assertTccl("stop", ctx.getBundle());
	}
	
	private void assertTccl(String operation, Bundle expectedBundle) throws BundleException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		
		System.out.println("ClassLoader during " + operation + ": " + classLoader);

		if (!(classLoader instanceof BundleClassLoader)) {
			throw new BundleException("TCCL " + classLoader + " was not a BundleClassLoader");
		}
		
		if (!expectedBundle.equals(((BundleReference)classLoader).getBundle())) {
			throw new BundleException("TCCL " + classLoader + " was not bundle class loader for this bundle");
		}
	}
}
