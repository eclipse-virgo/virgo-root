/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.services.accessor;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.service.command.Descriptor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;


public class ServicesAccessorCommandProvider {
	
	private final String CMD_HELP_MSG = "Shows exposed content in the framework\r\n" +  
			"   list_exposed_content <option> - display information about apis, which will be transparently added to applications, apis' implementations and JNDI consumption of services\r\n" +
			"      Options:\r\n" +
			"      -api - display all API bundles\r\n" +
	        "      -impl - display all Implementation bundles\r\n" +
	        "      -clash - display all clashing API/Implementation bundles and which one is chosen\r\n";
	
	private static String API_OPTION = "-api";
	private static String IMPL_OPTION = "-impl";
	private static String CLASH_OPTION = "-clash";
	private static String API_BUNDLE_TYPE = "API";
	private static String IMPL_BUNDLE_TYPE = "Implementation";
	
	private WebAppBundleTrackerCustomizer webAppBundleTrackerCustomizer;
	private WebAppBundleClassLoaderDelegateHook wabClassLoaderDelegateHook;
	private String bundleType;
	private final Object monitor = new Object();
	
	public void activate(BundleContext context) {
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("osgi.command.scope", "enterprise");
		properties.put("osgi.command.function", new String[] {"list_exposed_content"});
		context.registerService(ServicesAccessorCommandProvider.class, this, properties);
	}
	
	@Descriptor(CMD_HELP_MSG)
    public void list_exposed_content(String... option) {
        if (option != null && option.length == 1) {
            synchronized (monitor) {
                if (wabClassLoaderDelegateHook != null) {
                    if (API_OPTION.equalsIgnoreCase(option[0])) {
                        bundleType = API_BUNDLE_TYPE;
                        displayBundles(wabClassLoaderDelegateHook.getApiBundles());
                    } else if (IMPL_OPTION.equalsIgnoreCase(option[0])) {
                        bundleType = IMPL_BUNDLE_TYPE;
                        displayBundles(wabClassLoaderDelegateHook.getImplBundles());
                    } else if (CLASH_OPTION.equalsIgnoreCase(option[0])) {
                        findClashes();
                    }
                }
            }
        } else {
            System.out.println("Usage: list_exposed_content <option>");
            System.out.println("Specify -api, -impl or -clash option");
        }
    }
	
	private void displayBundles (Set<Bundle> bundles) {
		if (bundles.size() == 0) {
			System.out.println("No service " + bundleType + " bundles available");
			return;
		}
		
		System.out.println();
		System.out.print("ID");
		System.out.print("\t");
		System.out.print("Bundle name");
		System.out.println();
		
		for(Bundle bundle : bundles) {
			System.out.print(bundle.getBundleId());
			System.out.print("\t");
			System.out.print(bundle.getSymbolicName() + "_" + bundle.getVersion());
			System.out.println();
		}
	}
	
	private void findClashes () {
		Map<String, List<Bundle>> bundlesWithSameBSNMap = webAppBundleTrackerCustomizer.getBundlesWithSameBSNMap();
		if (bundlesWithSameBSNMap == null || bundlesWithSameBSNMap.size() == 0) {
			System.out.println("There are no clashes");
			return;
		}
		Set<Bundle> apiBundles = wabClassLoaderDelegateHook.getApiBundles();
	    handleClashes(apiBundles, bundlesWithSameBSNMap, API_BUNDLE_TYPE);
	    Set<Bundle> implBundles = wabClassLoaderDelegateHook.getImplBundles();
	    handleClashes(implBundles, bundlesWithSameBSNMap, IMPL_BUNDLE_TYPE);
	}
	
	private void handleClashes(Set<Bundle> bundles, Map<String, List<Bundle>> bundlesWithSameBSNMap, String type) {
		System.out.println("Clashing " + type + "s:");
		System.out.println();
		for (Bundle bundle : bundles) {
			List<Bundle> bundlesWithSameBSN = bundlesWithSameBSNMap.get(bundle.getSymbolicName());
			if (bundlesWithSameBSN.size() > 1) {
				printClashing(bundlesWithSameBSN, bundle);
			}
		}
	}
	
	private void printClashing(List<Bundle> bundlesWithSameBSN, Bundle chosenBundle) {
		System.out.println("Clashing bundles for BSN " + chosenBundle.getSymbolicName() + ":");
		for (Bundle bundle : bundlesWithSameBSN) {
			System.out.println(bundle.getSymbolicName() + "_" + bundle.getVersion());
		}
		System.out.println();
		System.out.println("Chosen bundle: " + chosenBundle.getSymbolicName() + "_" + chosenBundle.getVersion());
	}
	
	public void bindCustomizer(WebAppBundleClassloaderCustomizer customizer) {
		this.webAppBundleTrackerCustomizer = customizer.getWebAppBundleTrackerCustomizer();
		this.wabClassLoaderDelegateHook = customizer.getWebAppBundleClassLoaderDelegateHook();
	}
	
	public void unbindCustomizer(WebAppBundleClassloaderCustomizer customizer) {
		synchronized (monitor) {
			this.wabClassLoaderDelegateHook = null;
			this.webAppBundleTrackerCustomizer = null;
		}
	}

}
