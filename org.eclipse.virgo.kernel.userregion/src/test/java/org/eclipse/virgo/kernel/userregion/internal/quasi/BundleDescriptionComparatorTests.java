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

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.GenericDescription;
import org.eclipse.osgi.service.resolver.GenericSpecification;
import org.eclipse.osgi.service.resolver.HostSpecification;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.NativeCodeSpecification;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.virgo.kernel.userregion.internal.quasi.BundleDescriptionComparator;
import org.junit.Test;
import org.osgi.framework.Version;


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

        public boolean attachFragments() {
            throw new UnsupportedOperationException();
        }

        public boolean dynamicFragments() {
            throw new UnsupportedOperationException();
        }

        public long getBundleId() {
            throw new UnsupportedOperationException();
        }

        public State getContainingState() {
            throw new UnsupportedOperationException();
        }

        public BundleDescription[] getDependents() {
            throw new UnsupportedOperationException();
        }

        public String[] getExecutionEnvironments() {
            throw new UnsupportedOperationException();
        }

        public ExportPackageDescription[] getExportPackages() {
            throw new UnsupportedOperationException();
        }

        public BundleDescription[] getFragments() {
            throw new UnsupportedOperationException();
        }

        public GenericDescription[] getGenericCapabilities() {
            throw new UnsupportedOperationException();
        }

        public GenericSpecification[] getGenericRequires() {
            throw new UnsupportedOperationException();
        }

        public HostSpecification getHost() {
            throw new UnsupportedOperationException();
        }

        public ImportPackageSpecification[] getImportPackages() {
            throw new UnsupportedOperationException();
        }

        public String getLocation() {
            throw new UnsupportedOperationException();
        }

        public NativeCodeSpecification getNativeCodeSpecification() {
            throw new UnsupportedOperationException();
        }

        public String getPlatformFilter() {
            throw new UnsupportedOperationException();
        }

        public BundleSpecification[] getRequiredBundles() {
            throw new UnsupportedOperationException();
        }

        public ExportPackageDescription[] getResolvedImports() {
            throw new UnsupportedOperationException();
        }

        public BundleDescription[] getResolvedRequires() {
            throw new UnsupportedOperationException();
        }

        public ExportPackageDescription[] getSelectedExports() {
            throw new UnsupportedOperationException();
        }

        public ExportPackageDescription[] getSubstitutedExports() {
            throw new UnsupportedOperationException();
        }

        public String getSymbolicName() {
            return this.symbolicName;
        }

        public Object getUserObject() {
            throw new UnsupportedOperationException();
        }

        public boolean hasDynamicImports() {
            throw new UnsupportedOperationException();
        }

        public boolean isRemovalPending() {
            throw new UnsupportedOperationException();
        }

        public boolean isResolved() {
            throw new UnsupportedOperationException();
        }

        public boolean isSingleton() {
            throw new UnsupportedOperationException();
        }

        public void setUserObject(Object userObject) {
            throw new UnsupportedOperationException();            
        }
       
        public String getName() {
            throw new UnsupportedOperationException();
        }

        public BundleDescription getSupplier() {
            throw new UnsupportedOperationException();
        }

        public Version getVersion() {
            return this.version;
        }
    }
}
