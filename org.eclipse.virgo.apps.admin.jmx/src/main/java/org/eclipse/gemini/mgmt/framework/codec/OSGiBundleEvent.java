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

import org.osgi.framework.BundleEvent;
import org.osgi.jmx.framework.BundleStateMBean;

/**
 * <p>
 * This class represents the CODEC for the composite data representing a OSGi
 * <link>BundleEvent</link>
 * <p>
 * It serves as both the documentation of the type structure and as the
 * codification of the mechanism to convert to/from the CompositeData.
 * <p>
 * The structure of the composite data is:
 * <table border="1">
 * <tr>
 * <td>Identifier</td>
 * <td>long</td>
 * </tr>
 * <tr>
 * <td>location</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>SymbolicName</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>EventType</td>
 * <td>int</td>
 * </tr>
 * </table>
 */
public class OSGiBundleEvent {

	/**
	 * Construct an OSGiBundleEvent from the supplied <ling>BundleEvent</link>
	 * 
	 * @param event
	 *            - the event to represent
	 */
	public OSGiBundleEvent(BundleEvent event) {
		this(event.getBundle().getBundleId(), event.getBundle().getLocation(),
				event.getBundle().getSymbolicName(), event.getType());
	}

	/**
	 * Construct an OSGiBundleEvent from the CompositeData representing the
	 * event
	 * 
	 * @param data
	 *            - the CompositeData representing the event.
	 */
	@SuppressWarnings("boxing")
	public OSGiBundleEvent(CompositeData data) {
		bundleId = (Long) data.get(BundleStateMBean.IDENTIFIER);
		location = (String) data.get(BundleStateMBean.LOCATION);
		symbolicName = (String) data.get(BundleStateMBean.SYMBOLIC_NAME);
		eventType = (Integer) data.get(BundleStateMBean.EVENT);
	}

	/**
	 * Construct the OSGiBundleEvent
	 * 
	 * @param bundleId
	 * @param location
	 * @param symbolicName
	 * @param eventType
	 */
	public OSGiBundleEvent(long bundleId, String location, String symbolicName,
			int eventType) {
		this.bundleId = bundleId;
		this.location = location;
		this.symbolicName = symbolicName;
		this.eventType = eventType;
	}

	/**
	 * Answer the receiver encoded as CompositeData
	 * 
	 * @return the CompositeData encoding of the receiver.
	 */
	@SuppressWarnings("boxing")
	public CompositeData asCompositeData() {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(BundleStateMBean.IDENTIFIER, bundleId);
		items.put(BundleStateMBean.LOCATION, location);
		items.put(BundleStateMBean.SYMBOLIC_NAME, symbolicName);
		items.put(BundleStateMBean.EVENT, eventType);
		try {
			return new CompositeDataSupport(BundleStateMBean.BUNDLE_EVENT_TYPE,
					items);
		} catch (OpenDataException e) {
			throw new IllegalStateException(
					"Cannot form bundle event open data", e);
		}
	}

	/**
	 * @return the identifier of the bundle for this event
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
	 * @return the location of the bundle for this event
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @return the symbolic name of the bundle for this event
	 */
	public String getSymbolicName() {
		return symbolicName;
	}

	private long bundleId;

	private int eventType;

	private String location;

	private String symbolicName;

}
