/*******************************************************************************
 * Copyright (c) 2010 Oracle.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at 
 *     http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Hal Hildebrand - Initial JMX support 
 ******************************************************************************/

package org.eclipse.gemini.mgmt.framework;

import static org.osgi.framework.Constants.OBJECTCLASS;

import java.io.IOException;
import java.util.ArrayList;

import javax.management.Notification;
import javax.management.openmbean.TabularData;

import org.osgi.framework.AllServiceListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import org.osgi.jmx.framework.ServiceStateMBean;
import org.osgi.util.tracker.ServiceTracker;

import org.eclipse.gemini.mgmt.Monitor;
import org.eclipse.gemini.mgmt.codec.OSGiProperties;
import org.eclipse.gemini.mgmt.framework.codec.OSGiService;
import org.eclipse.gemini.mgmt.framework.codec.OSGiServiceEvent;

/** 
 * 
 */
public class ServiceState extends Monitor implements ServiceStateMBean {
	public ServiceState(BundleContext bc) {
		this.bc = bc;
	}

	public long getBundleIdentifier(long serviceId) throws IOException {
		return ref(serviceId).getBundle().getBundleId();
	}

	public TabularData getProperties(long serviceId) throws IOException {
		return OSGiProperties.tableFrom(ref(serviceId));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.ServiceStateMBean#getBundle(long)
	 */

	public String[] getObjectClass(long serviceId) throws IOException {
		return (String[]) ref(serviceId).getProperty(OBJECTCLASS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.ServiceStateMBean#listServices()
	 */

	public TabularData listServices() {
		ArrayList<OSGiService> services = new ArrayList<OSGiService>();
		for (Bundle bundle : bc.getBundles()) {
			ServiceReference[] refs = bundle.getRegisteredServices();
			if (refs != null) {
				for (ServiceReference ref : refs) {
					services.add(new OSGiService(ref));
				}
			}
		}
		return OSGiService.tableFrom(services);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.ServiceStateMBean#getServiceInterfaces(long)
	 */

	public long[] getUsingBundles(long serviceId) throws IOException {
		Bundle[] bundles = ref(serviceId).getUsingBundles();
		long[] ids = new long[bundles.length];
		for (int i = 0; i < bundles.length; i++) {
			ids[i] = bundles[i].getBundleId();
		}
		return ids;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.ServiceStateMBean#getServices()
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.Monitor#addListener()
	 */
	@Override
	protected void addListener() {
		serviceListener = getServiceListener();
		bc.addServiceListener(serviceListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.ServiceStateMBean#getUsingBundles(long)
	 */

	protected ServiceListener getServiceListener() {
		return new AllServiceListener() {
			public void serviceChanged(ServiceEvent serviceEvent) {
				Notification notification = new Notification(
						ServiceStateMBean.EVENT, objectName, sequenceNumber++);
				notification.setUserData(new OSGiServiceEvent(serviceEvent)
						.asCompositeData());
				sendNotification(notification);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.Monitor#removeListener()
	 */
	@Override
	protected void removeListener() {
		if (serviceListener != null) {
			bc.removeServiceListener(serviceListener);
		}
	}

	protected ServiceReference ref(long serviceId) throws IOException {
		Filter filter;
		try {
			filter = bc.createFilter("(" + Constants.SERVICE_ID + "="
					+ serviceId + ")");
		} catch (InvalidSyntaxException e) {
			throw new IOException("Invalid filter syntax: " + e);
		}
		ServiceTracker tracker = new ServiceTracker(bc, filter, null);
		tracker.open();
		ServiceReference serviceReference = tracker.getServiceReference();
		if (serviceReference == null) {
			throw new IOException("Service <" + serviceId + "> does not exist");
		}
		tracker.close();
		return serviceReference;
	}

	protected ServiceListener serviceListener;
	protected BundleContext bc;

}
