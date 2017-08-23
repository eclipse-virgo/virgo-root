package org.eclipse.virgo.management.fragment;

import static org.junit.Assert.assertEquals;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.easymock.EasyMock;
import org.eclipse.equinox.region.Region;
import org.eclipse.equinox.region.RegionDigraph;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class VirgoObjectNameTranslatorTests {

	private static final String MOCK_REGION_NAME = "testRegion";

	private ObjectName testObjectName;
	
	private ObjectName testObjectNameTranslation;

	private BundleContext mockContext;
	
	private ServiceReference<RegionDigraph> mockServiceReference;

	private Bundle mockBundle;

	private RegionDigraph mockRegionDigraph;

	private Region mockRegion;


	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws MalformedObjectNameException, NullPointerException{
		Hashtable<String, String> table = new Hashtable<String, String>();
		table.put("testKey", "testValue");
		testObjectName = ObjectName.getInstance("testDomain", table);
		table.put("region", MOCK_REGION_NAME);
		testObjectNameTranslation = ObjectName.getInstance("testDomain", table);
		mockContext = EasyMock.createMock(BundleContext.class);
		mockServiceReference = (ServiceReference<RegionDigraph>) EasyMock.createMock(ServiceReference.class);
        mockBundle = EasyMock.createMock(Bundle.class);
        mockRegionDigraph = EasyMock.createMock(RegionDigraph.class);
        mockRegion = EasyMock.createMock(Region.class);
		EasyMock.expect(mockContext.getServiceReference(RegionDigraph.class)).andReturn(mockServiceReference);
		EasyMock.expect(mockContext.getService(mockServiceReference)).andReturn(mockRegionDigraph);
		EasyMock.expect(mockContext.getBundle()).andReturn(mockBundle);
		EasyMock.expect(mockBundle.getBundleId()).andReturn(5l);
		EasyMock.expect(mockRegionDigraph.getRegion(5l)).andReturn(mockRegion);
		EasyMock.expect(mockRegion.getName()).andReturn(MOCK_REGION_NAME);
	}
	
    private void replayMocks() {
        EasyMock.replay(mockContext, mockServiceReference, mockBundle, mockRegionDigraph, mockRegion);
    }

    @After
    public void tearDown() {
        EasyMock.verify(mockContext, mockServiceReference, mockBundle, mockRegionDigraph, mockRegion);
    }
	
	@Test
	public void test() {
		replayMocks();
		VirgoObjectNameTranslator virgoObjectNameTranslator = new VirgoObjectNameTranslator(mockContext);
		ObjectName translatedObjectName = virgoObjectNameTranslator.translate(testObjectName);
		assertEquals(testObjectNameTranslation, translatedObjectName);
	}

}
