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

package org.eclipse.gemini.mgmt.codec;

import static org.osgi.framework.Constants.SERVICE_ID;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import java.util.HashSet;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.packageadmin.RequiredBundle;
import org.osgi.service.startlevel.StartLevel;

/**
 * Static utilities
 * 
 */
@SuppressWarnings("unchecked")
public class Util {
	/**
	 * Answer the bundle ids of the bundles
	 * 
	 * @param bundles
	 * @return the bundle ids of the bundles
	 */
	public static long[] bundleIds(Bundle[] bundles) {
		if (bundles == null) {
			return new long[0];
		}
		long[] ids = new long[bundles.length];
		for (int i = 0; i < bundles.length; i++) {
			ids[i] = bundles[i].getBundleId();
		}
		return ids;
	}

	/**
	 * Answer the bundle ids of the bundles
	 * 
	 * @param bundles
	 * @return the bundle ids of the bundles
	 */
	public static long[] bundleIds(RequiredBundle[] bundles) {
		if (bundles == null) {
			return new long[0];
		}
		long[] ids = new long[bundles.length];
		for (int i = 0; i < bundles.length; i++) {
			ids[i] = bundles[i].getBundle().getBundleId();
		}
		return ids;
	}

	public static long[] getDependencies(Bundle b, PackageAdmin admin) {
		Set<Long> dependencies = new HashSet<Long>();
		for (ExportedPackage pkg : admin.getExportedPackages((Bundle) null)) {
			if (!dependencies.contains(pkg.getExportingBundle().getBundleId())) {
				Bundle[] importing = pkg.getImportingBundles();
				if (importing != null) {
					for (Bundle bundle : importing) {
						if (bundle == b) {
							dependencies.add(pkg.getExportingBundle()
									.getBundleId());
							break;
						}
					}
				}
			}
		}
		return longArrayFrom(dependencies
				.toArray(new Long[dependencies.size()]));
	}

	/**
	 * Answer the the bundle ids of the bundles requiring the given bundles
	 * 
	 * @param b
	 * @param bc
	 * @param admin
	 * @return the the bundle ids of the bundles requireing the given bundles
	 */
	public static long[] getBundlesRequiring(Bundle b, BundleContext bc,
			PackageAdmin admin) {
		Bundle[] all = bc.getBundles();
		ArrayList<Long> requiring = new ArrayList<Long>();
		for (Bundle anAll : all) {
			long[] dependencies = getDependencies(anAll, admin);
			if (dependencies == null) {
				continue;
			}
			for (long r : dependencies) {
				if (r == b.getBundleId()) {
					requiring.add(anAll.getBundleId());
				}
			}
		}
		long[] ids = new long[requiring.size()];
		for (int i = 0; i < requiring.size(); i++) {
			ids[i] = requiring.get(i);
		}
		return ids;
	}

	/**
	 * Answer the string representation of the exported packages of the bundle
	 * 
	 * @param b
	 * @param admin
	 * @return the string representation of the exported packages of the bundle
	 */
	public static String[] getBundleExportedPackages(Bundle b,
			PackageAdmin admin) {
		ArrayList<String> packages = new ArrayList<String>();
		ExportedPackage[] exportedPackages = admin.getExportedPackages(b);
		if (exportedPackages == null) {
			return new String[0];
		}
		for (ExportedPackage pkg : exportedPackages) {
			packages.add(packageString(pkg));
		}
		return packages.toArray(new String[packages.size()]);
	}

	/**
	 * Answer the ids of the fragments hosted by the bundle
	 * 
	 * @param b
	 * @param admin
	 * @return the ids of the fragments hosted by the bundle
	 */
	public static long[] getBundleFragments(Bundle b, PackageAdmin admin) {
		Bundle[] fragments = admin.getFragments(b);
		if (fragments == null) {
			return new long[0];
		}
		long ids[] = new long[fragments.length];
		for (int i = 0; i < fragments.length; i++) {
			ids[i] = fragments[i].getBundleId();
		}
		return ids;
	}

	/**
	 * Answer the map of bundle headers
	 * 
	 * @param b
	 * @return the map of bundle headers
	 */
	public static Map<String, String> getBundleHeaders(Bundle b) {
		Map<String, String> headers = new Hashtable<String, String>();
		Dictionary h = b.getHeaders();
		for (Enumeration keys = h.keys(); keys.hasMoreElements();) {
			String key = (String) keys.nextElement();
			headers.put(key, (String) h.get(key));
		}
		return headers;
	}

	/**
	 * Answer the string representation of the packages imported by a bundle
	 * 
	 * @param b
	 * @param bc
	 * @param admin
	 * @return the string representation of the packages imported by a bundle
	 */
	public static String[] getBundleImportedPackages(Bundle b,
			BundleContext bc, PackageAdmin admin) {
		ArrayList<String> imported = new ArrayList<String>();
		Bundle[] allBundles = bc.getBundles();
		for (Bundle bundle : allBundles) {
			ExportedPackage[] eps = admin.getExportedPackages(bundle);
			if (eps == null) {
				continue;
			}
			for (ExportedPackage ep : eps) {
				Bundle[] imp = ep.getImportingBundles();
				if (imp == null) {
					continue;
				}
				for (Bundle b2 : imp) {
					if (b2.getBundleId() == b.getBundleId()) {
						imported.add(packageString(ep));
						break;
					}
				}
			}
		}
		if (imported.size() == 0) {
			return new String[0];
		} else {
			return imported.toArray(new String[imported.size()]);
		}

	}

	/**
	 * Answer the string representation of the bundle state
	 * 
	 * @param b
	 * @return the string representation of the bundle state
	 */
	public static String getBundleState(Bundle b) {
		switch (b.getState()) {
		case Bundle.ACTIVE:
			return "ACTIVE";
		case Bundle.INSTALLED:
			return "INSTALLED";
		case Bundle.RESOLVED:
			return "RESOLVED";
		case Bundle.STARTING:
			return "STARTING";
		case Bundle.STOPPING:
			return "STOPPING";
		case Bundle.UNINSTALLED:
			return "UNINSTALLED";
		default:
			return "UNKNOWN";
		}
	}

	/**
	 * Answer the required bundle of a bundle
	 * 
	 * @param bundle
	 * @param bc
	 * @param admin
	 * @return the required bundle of a bundle
	 */
	public static RequiredBundle getRequiredBundle(Bundle bundle,
			BundleContext bc, PackageAdmin admin) {
		Bundle[] all = bc.getBundles();
		for (Bundle anAll : all) {
			String symbolicName = anAll.getSymbolicName();
			if (symbolicName != null) {
				RequiredBundle[] requiring = admin
						.getRequiredBundles(symbolicName);
				if (requiring == null) {
					continue;
				}
				for (RequiredBundle r : requiring) {
					if (r.getBundle().equals(bundle)) {
						return r;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Answer true if the bundle is a fragment
	 * 
	 * @param bundle
	 * @param admin
	 * @return true if the bundle is a fragment
	 */
	public static boolean isBundleFragment(Bundle bundle, PackageAdmin admin) {
		return admin.getBundleType(bundle) == PackageAdmin.BUNDLE_TYPE_FRAGMENT;
	}

	/**
	 * Answer true if the bundle has been persitently started
	 * 
	 * @param bundle
	 * @param sl
	 * @return true if the bundle has been persitently started
	 */
	public static boolean isBundlePersistentlyStarted(Bundle bundle,
			StartLevel sl) {
		return bundle.getBundleId() == 0
				|| sl.isBundlePersistentlyStarted(bundle);
	}

	/**
	 * Answer true if the bundle is required
	 * 
	 * @param bundle
	 * @param bc
	 * @param admin
	 * @return true if the bundle is required
	 */
	public static boolean isBundleRequired(Bundle bundle, BundleContext bc,
			PackageAdmin admin) {
		return getRequiredBundle(bundle, bc, admin) != null;
	}

	/**
	 * Answer true if the bundle is pending removal
	 * 
	 * @param bundle
	 * @param bc
	 * @param admin
	 * @return true if the bundle is pending removal
	 */
	public static boolean isRequiredBundleRemovalPending(Bundle bundle,
			BundleContext bc, PackageAdmin admin) {
		RequiredBundle r = getRequiredBundle(bundle, bc, admin);
		return r != null && r.isRemovalPending();
	}

	/**
	 * Answer the string representation of the exported package
	 * 
	 * @param pkg
	 * @return the string representation of the exported package
	 */
	public static String packageString(ExportedPackage pkg) {
		return pkg.getName() + ";" + pkg.getVersion();
	}

	/**
	 * Answer the ids of the service references
	 * 
	 * @param refs
	 * @return the ids of the service references
	 */
	public static long[] serviceIds(ServiceReference[] refs) {
		if (refs == null) {
			return new long[0];
		}
		long[] ids = new long[refs.length];
		for (int i = 0; i < refs.length; i++) {
			ids[i] = (Long) refs[i].getProperty(SERVICE_ID);
		}
		return ids;
	}

	/**
	 * Answer a Long array from the supplied array of longs
	 * 
	 * @param array
	 * @return a Long array from the supplied array of longs
	 */
	public static Long[] LongArrayFrom(long[] array) {
		if (array == null) {
			return new Long[0];
		}
		Long[] result = new Long[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	/**
	 * Answer an array of longs from an array of Longs
	 * 
	 * @param array
	 * @return an array of longs from an array of Longs
	 */
	public static long[] longArrayFrom(Long[] array) {
		if (array == null) {
			return new long[0];
		}
		long[] result = new long[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	/**
	 * The type for an array of longs
	 */
	public static ArrayType LONG_ARRAY_TYPE;

	/**
	 * The type for an array of strings
	 */
	public static ArrayType STRING_ARRAY_TYPE;

	private static final Logger log = Logger.getLogger(Util.class
			.getCanonicalName());

	static {
		try {
			LONG_ARRAY_TYPE = new ArrayType(1, SimpleType.LONG);
			STRING_ARRAY_TYPE = new ArrayType(1, SimpleType.STRING);
		} catch (OpenDataException e) {
			log.log(Level.SEVERE, "Cannot create array open data type", e);
		}
	}
}
