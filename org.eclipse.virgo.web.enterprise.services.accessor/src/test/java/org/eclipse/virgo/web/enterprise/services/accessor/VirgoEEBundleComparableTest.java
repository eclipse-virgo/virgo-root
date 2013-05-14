package org.eclipse.virgo.web.enterprise.services.accessor;

import static org.junit.Assert.*;

import java.util.Set;
import java.util.TreeSet;

import org.easymock.EasyMock;
import org.junit.Test;
import org.osgi.framework.Bundle;

public class VirgoEEBundleComparableTest {
	
private static final String BUNDLE2 = "bundle2";
private static final String BUNDLE1 = "bundle1";
private static final String ORG_ECLIPSE_PERSISTENCE_CORE = "org.eclipse.persistence.core";
private static final String ORG_ECLILSE_PERSISTENCE_JPA = "org.eclipse.persistence.jpa";
private static final String ORG_APACHE_WEBBEANS_WEB = "org.apache.openwebbeans.web";
private static final String ORG_APACHE_WEBBEANS_IMPL = "org.apache.openwebbeans.impl";
private static final String ORG_GLASSFISH_COM_SUN_FACES = "org.glassfish.com.sun.faces";
private static final String OPENEJB_CORE = "org.apache.openejb.core";
private static final String ORG_APACHE_WEBBEANS_JSF = "org.apache.openwebbeans.jsf";

//"a;bundle-version\\=1.0.0,b;bundle-version\\=1.0.0")
	@Test
	public void testCompareTwoNotSpecialBundles() {
		System.clearProperty(WebAppBundleTrackerCustomizer.IMPL_BUNDLES);
		VirgoEEBundleComparable comparable = new VirgoEEBundleComparable();
	
		Bundle b1 = EasyMock.createMock(Bundle.class);
		Bundle b2 = EasyMock.createMock(Bundle.class);
	
		EasyMock.expect(b1.getSymbolicName()).andReturn("a");
		EasyMock.expect(b2.getSymbolicName()).andReturn("b");
		
		EasyMock.replay(b1, b2);
		
		assertEquals("When not matching impl.bundles bundles should be sorted alphabetically",-1, comparable.compare(b1, b2));
	}
	
	@Test
	public void testEqualNotBundles() {
		System.clearProperty(WebAppBundleTrackerCustomizer.IMPL_BUNDLES);
		VirgoEEBundleComparable comparable = new VirgoEEBundleComparable();
	
		Bundle b1 = EasyMock.createMock(Bundle.class);
		EasyMock.expect(b1.getSymbolicName()).andReturn("a");

		EasyMock.replay(b1);
		
		assertEquals("Exact bundles comparison is wrong", 0, comparable.compare(b1, b1));
	}
	
	@Test
	public void testTwoBundlesSameSymbolicNames() {
		System.clearProperty(WebAppBundleTrackerCustomizer.IMPL_BUNDLES);
		VirgoEEBundleComparable comparable = new VirgoEEBundleComparable();
	
		Bundle b1 = EasyMock.createMock(Bundle.class);
		Bundle b2 = EasyMock.createMock(Bundle.class);
	
		EasyMock.expect(b1.getSymbolicName()).andReturn("a");
		EasyMock.expect(b2.getSymbolicName()).andReturn("a");
		
		EasyMock.replay(b1, b2);
		
		assertEquals("Bundles have same symbolic name, should be equal",0, comparable.compare(b1, b2));
	}
	

	@Test
	public void testSpecialToNotSpecialBundles() {
		System.setProperty(WebAppBundleTrackerCustomizer.IMPL_BUNDLES, "b;bundle-version\\=1.0.0");
		VirgoEEBundleComparable comparable = new VirgoEEBundleComparable();
	
		Bundle b1 = EasyMock.createMock(Bundle.class);
		Bundle b2 = EasyMock.createMock(Bundle.class);
	
		EasyMock.expect(b1.getSymbolicName()).andReturn("a");
		EasyMock.expect(b2.getSymbolicName()).andReturn("b");
		
		EasyMock.replay(b1, b2);
		
		assertEquals("Bundle b is declared as impl - should be less", -1, comparable.compare(b2, b1));
	}
	
	@Test
	public void testTwoSpecialBundles() {
		System.setProperty(WebAppBundleTrackerCustomizer.IMPL_BUNDLES, "b;bundle-version\\=1.0.0,a;bundle-version\\=1.0.0");
		VirgoEEBundleComparable comparable = new VirgoEEBundleComparable();
	
		Bundle b1 = EasyMock.createMock(Bundle.class);
		Bundle b2 = EasyMock.createMock(Bundle.class);
	
		EasyMock.expect(b1.getSymbolicName()).andReturn("a");
		EasyMock.expect(b2.getSymbolicName()).andReturn("b");
		
		EasyMock.replay(b1, b2);
		
		assertEquals("Bundle b is declared first in impl.bundles - should be less",-1, comparable.compare(b2, b1));
	}
	
	@Test
	public void realScenarioSimulation() {
		System.setProperty(WebAppBundleTrackerCustomizer.IMPL_BUNDLES, "org.apache.openejb.core;bundle-version\\=4.5.2.SNAPSHOT,org.apache.openwebbeans.impl;bundle-version\\=1.1.7,org.glassfish.com.sun.faces;bundle-version\\=2.1.6.v201205171319-sap-3,org.apache.openwebbeans.jsf;bundle-version\\=1.1.6,org.eclipse.persistence.jpa;bundle-version\\=2.4.1.v20121003-ad44345,org.eclipse.persistence.core;bundle-version\\=2.4.1.v20121003-ad44345,org.apache.openwebbeans.web;bundle-version\\=1.1.7");
		
		Bundle openejb = EasyMock.createMock(Bundle.class);
		Bundle jsf = EasyMock.createMock(Bundle.class);
		Bundle webbeans_impl = EasyMock.createMock(Bundle.class);
		Bundle persistence_jpa = EasyMock.createMock(Bundle.class);
		Bundle persistence_core = EasyMock.createMock(Bundle.class);
		Bundle webbeans_web = EasyMock.createMock(Bundle.class);
		Bundle bundle1 = EasyMock.createMock(Bundle.class);
		Bundle bundle2 = EasyMock.createMock(Bundle.class);
		Bundle webbeans_jsf = EasyMock.createMock(Bundle.class);
	
		EasyMock.expect(openejb.getSymbolicName()).andReturn(OPENEJB_CORE).anyTimes();
		EasyMock.expect(jsf.getSymbolicName()).andReturn(ORG_GLASSFISH_COM_SUN_FACES).anyTimes();
		EasyMock.expect(webbeans_impl.getSymbolicName()).andReturn(ORG_APACHE_WEBBEANS_IMPL).anyTimes();		
		EasyMock.expect(webbeans_web.getSymbolicName()).andReturn(ORG_APACHE_WEBBEANS_WEB).anyTimes();
		EasyMock.expect(persistence_jpa.getSymbolicName()).andReturn(ORG_ECLILSE_PERSISTENCE_JPA).anyTimes();		
		EasyMock.expect(persistence_core.getSymbolicName()).andReturn(ORG_ECLIPSE_PERSISTENCE_CORE).anyTimes();
		EasyMock.expect(bundle1.getSymbolicName()).andReturn(BUNDLE1).anyTimes();
		EasyMock.expect(bundle2.getSymbolicName()).andReturn(BUNDLE2).anyTimes();
		EasyMock.expect(webbeans_jsf.getSymbolicName()).andReturn(ORG_APACHE_WEBBEANS_JSF).anyTimes();

		Set<Bundle> sortedBundlesSet = new TreeSet<Bundle>(new VirgoEEBundleComparable());
		
		EasyMock.replay(openejb, jsf, persistence_jpa, persistence_core, webbeans_web, webbeans_impl, webbeans_jsf, bundle1, bundle2);
		
		sortedBundlesSet.add(bundle2);
		sortedBundlesSet.add(bundle1);
		sortedBundlesSet.add(webbeans_web);
		sortedBundlesSet.add(persistence_core);
		sortedBundlesSet.add(persistence_jpa);
		sortedBundlesSet.add(jsf);
		sortedBundlesSet.add(openejb);
		sortedBundlesSet.add(webbeans_impl);
		sortedBundlesSet.add(webbeans_jsf);
		
		Bundle[] sortedBundles = sortedBundlesSet.toArray(new Bundle[sortedBundlesSet.size()]);
		assertEquals(OPENEJB_CORE, sortedBundles[0].getSymbolicName());		
		assertEquals(ORG_APACHE_WEBBEANS_IMPL, sortedBundles[1].getSymbolicName());
		assertEquals(ORG_GLASSFISH_COM_SUN_FACES, sortedBundles[2].getSymbolicName());
		assertEquals(ORG_APACHE_WEBBEANS_JSF, sortedBundles[3].getSymbolicName());
		assertEquals(ORG_ECLILSE_PERSISTENCE_JPA, sortedBundles[4].getSymbolicName());
		assertEquals(ORG_ECLIPSE_PERSISTENCE_CORE, sortedBundles[5].getSymbolicName());
		assertEquals(ORG_APACHE_WEBBEANS_WEB, sortedBundles[6].getSymbolicName());	
		assertEquals(BUNDLE1, sortedBundles[7].getSymbolicName());
		assertEquals(BUNDLE2, sortedBundles[8].getSymbolicName());
		
	}
	

}