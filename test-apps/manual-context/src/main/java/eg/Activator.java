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
import org.springframework.context.support.*;

public class Activator implements BundleActivator {
	
	public void start(BundleContext ctx) throws BundleException {
		System.out.println("Starting with TCCL: " + Thread.currentThread().getContextClassLoader());
		ClassPathXmlApplicationContext c = new ClassPathXmlApplicationContext("context.xml", getClass());
		c.refresh();
	}
	
	public void stop(BundleContext ctx) throws BundleException {
		
	}
}
