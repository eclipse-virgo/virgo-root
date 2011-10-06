package org.eclipse.gemini.mgmt.framework;

import java.io.IOException;

import javax.management.openmbean.TabularData;

import org.osgi.jmx.framework.BundleStateMBean;

public interface CustomBundleStateMBean extends BundleStateMBean{

	/**
	 * Mask for listBundles method, that requires bundle location attribute to be 
	 * included in the returned data.
	 * 
	 */
	public final static int LOCATION = 1;
	
	/**
	 * Mask for listBundles method, that requires bundle id attribute to be 
	 * included in the returned data.
	 */
	public final static int IDENTIFIER = 2;
	
	/**
	 * Mask for listBundles method, that requires bundle symbolic name attribute to be 
	 * included in the returned data.
	 */
	public final static int SYMBOLIC_NAME = 2 << 1;
	
	/**
	 * Mask for listBundles method, that requires bundle version attribute to be 
	 * included in the returned data.
	 */
	public final static int VERSION = 2 << 2;
	
	/**
	 * Mask for listBundles method, that requires bundle start level attribute to be 
	 * included in the returned data.
	 */
	public final static int START_LEVEL = 2 << 3;
	
	/**
	 * Mask for listBundles method, that requires bundle state attribute to be 
	 * included in the returned data.
	 */
	public final static int STATE = 2 << 4;
	
	/**
	 * Mask for listBundles method, that requires bundle last modified attribute to be 
	 * included in the returned data.
	 */
	public final static int LAST_MODIFIED = 2 << 5;
	
	/**
	 * Mask for listBundles method, that requires bundle persistently started attribute to be 
	 * included in the returned data. 
	 */
	public final static int PERSISTENTLY_STARTED = 2 << 6;
	
	/**
	 * Mask for listBundles method, that requires bundle removal pending attribute to be 
	 * included in the returned data.
	 */
	public final static int REMOVAL_PENDING = 2 << 7;
	
	/**
	 * Mask for listBundles method, that requires bundle required attribute to be 
	 * included in the returned data.
	 */
	public final static int REQUIRED = 2 << 8;
	
	/**
	 * Mask for listBundles method, that requires bundle fragment attribute to be 
	 * included in the returned data.
	 */
	public final static int FRAGMENT = 2 << 9;
	
	/**
	 * Mask for listBundles method, that requires bundle registered services attribute to be 
	 * included in the returned data.
	 */
	public final static int REGISTERED_SERVICES = 2 << 10;
	
	/**
	 * Mask for listBundles method, that requires bundle services in use attribute to be 
	 * included in the returned data.
	 */
	public final static int SERVICES_IN_USE = 2 << 11;
	
	/**
	 * Mask for listBundles method, that requires bundle headers attribute to be 
	 * included in the returned data. 
	 */
	public final static int HEADERS = 2 << 12;
	
	/**
	 * Mask for listBundles method, that requires bundle exported packages attribute to be 
	 * included in the returned data.
	 */
	public final static int EXPORTED_PACKAGES = 2 << 13;
	
	/**
	 * Mask for listBundles method, that requires bundle imported packages attribute to be 
	 * included in the returned data.
	 */
	public final static int IMPORTED_PACKAGES = 2 << 14;
	
	/**
	 * Mask for listBundles method, that requires bundle fragments attribute to be 
	 * included in the returned data.
	 */
	public final static int FRAGMENTS = 2 << 15;
	
	/**
	 * Mask for listBundles method, that requires bundle hosts attribute to be 
	 * included in the returned data.
	 */
	public final static int HOSTS = 2 << 16;
	
	/**
	 * Mask for listBundles method, that requires bundle "requiring bundles" attribute to be 
	 * included in the returned data.
	 */
	public final static int REQUIRING_BUNDLES = 2 << 17;
	
	/**
	 * Mask for listBundles method, that requires bundle "required bundles" attribute to be 
	 * included in the returned data.
	 */
	public final static int REQUIRED_BUNDLES = 2 << 18;
	
	/**
	 * Mask for listBundles method, that returns all available data. Equivalent to listBundles()
	 */
	public final static int DEFAULT = LOCATION + IDENTIFIER
	+ SYMBOLIC_NAME + VERSION + START_LEVEL + STATE + LAST_MODIFIED 
	+ PERSISTENTLY_STARTED + REMOVAL_PENDING + REQUIRED + FRAGMENT + REGISTERED_SERVICES
	+ SERVICES_IN_USE + HEADERS + EXPORTED_PACKAGES + IMPORTED_PACKAGES + FRAGMENTS 
	+ HOSTS + REQUIRING_BUNDLES + REQUIRED_BUNDLES;

	/**
	 * Answer the bundle state of the system in tabular form depending on the mask.
	 * 
	 * Each row of the returned table represents a single bundle. The Tabular
	 * Data consists of Composite Data that is type by {@link #BUNDLES_TYPE}.
	 *
	 * @param mask - representing the information that will be contained in the result
	 * @return the tabular representation of the bundle state
	 * @throws IOException
	 */
	TabularData listBundles(int mask) throws IOException;
}
