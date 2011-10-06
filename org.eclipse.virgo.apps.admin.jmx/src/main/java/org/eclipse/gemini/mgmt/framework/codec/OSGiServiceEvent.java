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

package org.eclipse.gemini.mgmt.framework.codec;

import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.jmx.framework.ServiceStateMBean;

/**
 * <p>
 * This class represents the CODEC for the composite data representing a OSGi
 * <link>ServiceEvent</link>
 * <p>
 * It serves as both the documentation of the type structure and as the
 * codification of the mechanism to convert to/from the CompositeData.
 * <p>
 * The structure of the composite data is:
 * <table border="1">
 * <tr>
 * <td>Identifier</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>BundleIdentifier</td>
 * <td>long</td>
 * </tr>
 * <tr>
 * <td>BundleLocation</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>ObjectClass</td>
 * <td>Array of String</td>
 * </tr>
 * <tr>
 * <td>EventType</td>
 * <td>int</td>
 * </tr>
 * </table>
 */
public class OSGiServiceEvent {

	/**
	 * Construct an OSGiServiceEvent from the CompositeData representing the
	 * event
	 * 
	 * @param data
	 *            = the CompositeData representation of the event
	 */
	@SuppressWarnings("boxing")
	public OSGiServiceEvent(CompositeData data) {
		serviceId = (Long) data.get(ServiceStateMBean.IDENTIFIER);
		bundleId = (Long) data.get(ServiceStateMBean.BUNDLE_IDENTIFIER);
		location = (String) data.get(ServiceStateMBean.BUNDLE_LOCATION);
		symbolicName = (String) data
				.get(ServiceStateMBean.BUNDLE_SYMBOLIC_NAME);
		interfaces = (String[]) data.get(ServiceStateMBean.OBJECT_CLASS);
		eventType = (Integer) data.get(ServiceStateMBean.EVENT);
	}

	/**
	 * Construct and OSGiServiceEvent
	 * 
	 * @param serviceId
	 * @param bundleId
	 * @param location
	 * @param symbolicName
	 * @param interfaces
	 * @param eventType
	 */
	public OSGiServiceEvent(long serviceId, long bundleId, String location,
			String symbolicName, String[] interfaces, int eventType) {
		this.serviceId = serviceId;
		this.bundleId = bundleId;
		this.location = location;
		this.symbolicName = symbolicName;
		this.interfaces = interfaces;
		this.eventType = eventType;
	}

	/**
	 * 
	 * Construct and OSGiServiceEvent from the original
	 * <link>ServiceEvent</link>
	 * 
	 * @param event
	 */
	@SuppressWarnings("boxing")
	public OSGiServiceEvent(ServiceEvent event) {
		this((Long) event.getServiceReference().getProperty(
				Constants.SERVICE_ID), event.getServiceReference().getBundle()
				.getBundleId(), event.getServiceReference().getBundle()
				.getLocation(), event.getServiceReference().getBundle()
				.getSymbolicName(), (String[]) event.getServiceReference()
				.getProperty(Constants.OBJECTCLASS), event.getType());
	}

	/**
	 * Answer the receiver encoded as CompositeData
	 * 
	 * @return the CompositeData encoding of the receiver.
	 */
	@SuppressWarnings("boxing")
	public CompositeData asCompositeData() {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(ServiceStateMBean.IDENTIFIER, serviceId);
		items.put(ServiceStateMBean.BUNDLE_IDENTIFIER, bundleId);
		items.put(ServiceStateMBean.BUNDLE_LOCATION, location);
		items.put(ServiceStateMBean.BUNDLE_SYMBOLIC_NAME, symbolicName);
		items.put(ServiceStateMBean.IDENTIFIER, serviceId);
		items.put(ServiceStateMBean.OBJECT_CLASS, interfaces);
		items.put(ServiceStateMBean.EVENT, eventType);

		try {
			return new CompositeDataSupport(
					ServiceStateMBean.SERVICE_EVENT_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException(
					"Cannot form service event open data", e);
		}
	}

	/**
	 * @return the identifier of the bundle the service belongs to
	 */
	public long getBundleId() {
		return bundleId;
	}

	/**
	 * @return the type of the event
	 */
	public int getEventType() {
		return eventType;
	}

	/**
	 * @return the interfaces the service implements
	 */
	public String[] getInterfaces() {
		return interfaces;
	}

	/**
	 * @return the location of the bundle the service belongs to
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @return the identifier of the service
	 */
	public long getServiceId() {
		return serviceId;
	}

	private long bundleId;
	private int eventType;
	private String[] interfaces;
	private String location;
	private String symbolicName;
	private long serviceId;

}
