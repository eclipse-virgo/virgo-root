/*******************************************************************************
 * Copyright (c) 2008, 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.kernel.userregion.internal.management;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiRequiredBundle;

public class JMXQuasiWire {

	private final String namespace;
	
	private final long providerId;
	
	private final long requirerId;
	
	private final Map<String, Object> bundleCapabilityAttributes;
	
	private final Map<String, Object> bundleCapabilityDirectives;
	
	private final Map<String, Object> bundleRequirementAttributes;
	
	private final Map<String, Object> bundleRequirementDirectives;
	
	public JMXQuasiWire(QuasiImportPackage quasiImportPackage) {
		this.namespace = BundleDescription.PACKAGE_NAMESPACE;
		this.providerId = quasiImportPackage.getProvider().getExportingBundle().getBundleId();
		this.requirerId = quasiImportPackage.getImportingBundle().getBundleId();
		this.bundleCapabilityAttributes = quasiImportPackage.getProvider().getAttributes();
		this.bundleCapabilityDirectives = quasiImportPackage.getProvider().getDirectives();
		this.bundleRequirementAttributes = quasiImportPackage.getAttributes();
		this.bundleRequirementDirectives = quasiImportPackage.getDirectives();
	}
	
	public JMXQuasiWire(QuasiBundle provider, QuasiBundle requirer, String hostNamespace) {
		this.namespace = hostNamespace;
		this.providerId = provider.getBundleId();
		this.requirerId = requirer.getBundleId();
		this.bundleCapabilityAttributes = this.getProperties();
		this.bundleCapabilityDirectives = this.getProperties();
		this.bundleRequirementAttributes = this.getProperties(new String[]{"host", provider.getSymbolicName()}, new String[]{"bundle-version", provider.getVersion().toString()});
		this.bundleRequirementDirectives = this.getProperties();
	}

	public JMXQuasiWire(QuasiRequiredBundle provider,QuasiBundle requirer, String bundleNamespace) {
		this.namespace = bundleNamespace;
		QuasiBundle quasiProvider = provider.getProvider();
		if(quasiProvider == null){
			this.providerId = -1l;
		}else{
			this.providerId = quasiProvider.getBundleId();
		}
		this.requirerId = requirer.getBundleId();
		this.bundleCapabilityAttributes = provider.getAttributes();
		this.bundleCapabilityDirectives = provider.getDirectives();
		this.bundleRequirementAttributes = this.getProperties(new String[]{"Required-Bundle", provider.getRequiredBundleName()}, new String[]{"Version-Constraint", provider.getVersionConstraint().toString()});
		this.bundleRequirementDirectives = this.getProperties();
	}

	public final long RequirerBundleId(){
		return this.requirerId;
	}
	
	public final long ProviderBundleId(){
		return this.providerId;
	}
	
	public final String Namespace(){
		return this.namespace;
	}
	
	public final Map<String, Object> BundleCapabilityAttributes(){
		return this.bundleCapabilityAttributes;
	}

	public final Map<String, Object> BundleCapabilityDirectives(){
		return this.bundleCapabilityDirectives;
	}
	
	public final Map<String, Object> BundleRequirementAttributes(){
		return this.bundleRequirementAttributes;
	}
	
	public final Map<String, Object> BundleRequirementDirectives(){
		return this.bundleRequirementDirectives;
	}
	
	private final Map<String, Object> getProperties(String[]... strings){
		Map<String, Object> properties = new HashMap<String, Object>();
		for (int i = 0; i < strings.length; i++) {
			properties.put(strings[i][0], strings[i][1]);
		}
		return properties;
	}
	
}
