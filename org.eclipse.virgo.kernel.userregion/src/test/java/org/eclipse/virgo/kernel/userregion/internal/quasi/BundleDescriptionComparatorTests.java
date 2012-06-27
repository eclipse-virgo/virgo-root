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

package org.eclipse.virgo.kernel.userregion.internal.quasi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.GenericDescription;
import org.eclipse.osgi.service.resolver.GenericSpecification;
import org.eclipse.osgi.service.resolver.HostSpecification;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.NativeCodeSpecification;
import org.eclipse.osgi.service.resolver.State;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;


public class BundleDescriptionComparatorTests {
    
    private final BundleDescriptionComparator bundleDescriptionComparator = new BundleDescriptionComparator();
    
    @Test
    public void matchingBundleSymbolicNameAndVersion() {
        StubBundleDescription bd1 = new StubBundleDescription("foo", new Version(1,2,3));
        StubBundleDescription bd2 = new StubBundleDescription("foo", new Version(1,2,3));
        assertEquals(0, bundleDescriptionComparator.compare(bd1, bd2));
    }
    
    @Test
    public void differingBundleSymbolicName() {
        StubBundleDescription bd1 = new StubBundleDescription("foo", new Version(1,2,3));
        StubBundleDescription bd2 = new StubBundleDescription("bar", new Version(1,2,3));
        
        assertDifferent(bd1, bd2);                                
    }
    
    @Test
    public void differingVersion() {
        StubBundleDescription bd1 = new StubBundleDescription("foo", new Version(1,2,3));
        StubBundleDescription bd2 = new StubBundleDescription("foo", new Version(2,3,4));
        
        assertDifferent(bd1, bd2);                                
    }
    
    @Test
    public void differingBundleSymbolicNameAndVersion() {
        StubBundleDescription bd1 = new StubBundleDescription("foo", new Version(1,2,3));
        StubBundleDescription bd2 = new StubBundleDescription("bar", new Version(2,3,4));
        
        assertDifferent(bd1, bd2);                                
    }
    
    private void assertDifferent(StubBundleDescription bd1, StubBundleDescription bd2) {
        int bd1ToBd2Comparison = this.bundleDescriptionComparator.compare(bd1, bd2);        
        assertTrue(bd1ToBd2Comparison != 0);
        
        int bd2ToBd1Comparison = this.bundleDescriptionComparator.compare(bd2, bd1);        
        assertTrue(bd2ToBd1Comparison != 0);
        
        assertFalse(bd2ToBd1Comparison < 0 && bd1ToBd2Comparison < 0);
    }
        
    private static final class StubBundleDescription implements BundleDescription {
        
        private final String symbolicName;
        
        private final Version version;
        
        StubBundleDescription(String symbolicName, Version version) {
            this.symbolicName = symbolicName;
            this.version = version;
        }

        @Override
        public boolean attachFragments() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean dynamicFragments() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getBundleId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public State getContainingState() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BundleDescription[] getDependents() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String[] getExecutionEnvironments() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ExportPackageDescription[] getExportPackages() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BundleDescription[] getFragments() {
            throw new UnsupportedOperationException();
        }

        @Override
        public GenericDescription[] getGenericCapabilities() {
            throw new UnsupportedOperationException();
        }

        @Override
        public GenericSpecification[] getGenericRequires() {
            throw new UnsupportedOperationException();
        }

        @Override
        public HostSpecification getHost() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ImportPackageSpecification[] getImportPackages() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getLocation() {
            throw new UnsupportedOperationException();
        }

        @Override
        public NativeCodeSpecification getNativeCodeSpecification() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getPlatformFilter() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BundleSpecification[] getRequiredBundles() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ExportPackageDescription[] getResolvedImports() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BundleDescription[] getResolvedRequires() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ExportPackageDescription[] getSelectedExports() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ExportPackageDescription[] getSubstitutedExports() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getSymbolicName() {
            return this.symbolicName;
        }

        @Override
        public Object getUserObject() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasDynamicImports() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRemovalPending() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isResolved() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isSingleton() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setUserObject(Object userObject) {
            throw new UnsupportedOperationException();            
        }
       
        @Override
        public String getName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BundleDescription getSupplier() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Version getVersion() {
            return this.version;
        }

        @Override
        public Map<String, String> getDeclaredDirectives() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, Object> getDeclaredAttributes() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<BundleCapability> getDeclaredCapabilities(String namespace) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getTypes() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bundle getBundle() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ImportPackageSpecification[] getAddedDynamicImportPackages() {
            throw new UnsupportedOperationException();
        }

        @Override
        public GenericDescription[] getSelectedGenericCapabilities() {
            throw new UnsupportedOperationException();
        }

        @Override
        public GenericDescription[] getResolvedGenericRequires() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BundleCapability getCapability() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, Object> getAttributes() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<BundleRequirement> getDeclaredRequirements(String namespace) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BundleWiring getWiring() {
            throw new UnsupportedOperationException();
        }

		@Override
		public List<Capability> getCapabilities(String arg0) {
			return null;
		}

		@Override
		public List<Requirement> getRequirements(String arg0) {
			return null;
		}
    }
}
