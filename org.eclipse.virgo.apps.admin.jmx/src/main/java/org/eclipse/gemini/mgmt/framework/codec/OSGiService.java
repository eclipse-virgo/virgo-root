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

import static org.osgi.framework.Constants.OBJECTCLASS;
import static org.osgi.framework.Constants.SERVICE_ID;
import static org.eclipse.gemini.mgmt.codec.Util.LongArrayFrom;
import static org.eclipse.gemini.mgmt.codec.Util.longArrayFrom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;

import org.osgi.framework.ServiceReference;
import org.eclipse.gemini.mgmt.codec.Util;
import org.osgi.jmx.framework.ServiceStateMBean;

/**
 * <p>
 * This class represents the CODEC for the composite data representing an OSGi
 * <link>ServiceReference</link>
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
 * <td>ObjectClass</td>
 * <td>Array of String</td>
 * </tr>
 * <tr>
 * <td>BundleIdentifier</td>
 * <td>long</td>
 * </tr>
 * <tr>
 * <td>UsingBundles</td>
 * <td>Array of long</td>
 * </tr>
 * </table>
 */
public class OSGiService {

	/**
	 * Construct an OSGiService encoded in the <link>CompositeData</link>
	 * 
	 * @param data
	 *            - the <link>CompositeData</link> encoding the OSGiService
	 */
	@SuppressWarnings("boxing")
	public OSGiService(CompositeData data) {
		this((Long) data.get(ServiceStateMBean.IDENTIFIER), (String[]) data
				.get(ServiceStateMBean.OBJECT_CLASS), (Long) data
				.get(ServiceStateMBean.BUNDLE_IDENTIFIER),
				longArrayFrom((Long[]) data
						.get(ServiceStateMBean.USING_BUNDLES)));
	}

	/**
	 * Construct an OSGiService
	 * 
	 * @param identifier
	 * @param interfaces
	 * @param properties
	 * @param bundle
	 * @param usingBundles
	 */
	public OSGiService(long identifier, String[] interfaces, long bundle,
			long[] usingBundles) {
		this.identifier = identifier;
		this.interfaces = interfaces;
		this.bundle = bundle;
		this.usingBundles = usingBundles;
	}

	/**
	 * Construct an OSGiService from the underlying
	 * <link>ServiceReference</link>
	 * 
	 * @param reference
	 *            - the reference of the service
	 */
	@SuppressWarnings("boxing")
	public OSGiService(ServiceReference reference) {
		this((Long) reference.getProperty(SERVICE_ID), (String[]) reference
				.getProperty(OBJECTCLASS), reference.getBundle().getBundleId(),
				Util.bundleIds(reference.getUsingBundles()));
	}

	/**
	 * Construct the TabularData representing a list of services
	 * 
	 * @param services
	 *            - the list of services
	 * 
	 * @return the TabularData representing the list of OSGiServices
	 */
	public static TabularData tableFrom(ArrayList<OSGiService> services) {
		TabularDataSupport table = new TabularDataSupport(
				ServiceStateMBean.SERVICES_TYPE);
		for (OSGiService service : services) {
			table.put(service.asCompositeData());
		}
		return table;
	}

	/**
	 * Answer the receiver encoded as CompositeData
	 * 
	 * @return the CompositeData encoding of the receiver.
	 */
	@SuppressWarnings("boxing")
	public CompositeData asCompositeData() {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(ServiceStateMBean.IDENTIFIER, identifier);
		items.put(ServiceStateMBean.OBJECT_CLASS, interfaces);
		items.put(ServiceStateMBean.BUNDLE_IDENTIFIER, bundle);
		items.put(ServiceStateMBean.USING_BUNDLES, LongArrayFrom(usingBundles));

		try {
			return new CompositeDataSupport(ServiceStateMBean.SERVICE_TYPE,
					items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form service open data", e);
		}
	}

	/**
	 * @return the identifier of the bundle the service belongs to
	 */
	public long getBundle() {
		return bundle;
	}

	/**
	 * @return the identifier of the service
	 */
	public long getIdentifier() {
		return identifier;
	}

	/**
	 * @return the interfaces implemented by the service
	 */
	public String[] getInterfaces() {
		return interfaces;
	}

	/**
	 * @return the identifiers of the bundles which are using the service
	 */
	public long[] getUsingBundles() {
		return usingBundles;
	}

	private long bundle;
	private long identifier;
	private String[] interfaces;
	private long[] usingBundles;
}
