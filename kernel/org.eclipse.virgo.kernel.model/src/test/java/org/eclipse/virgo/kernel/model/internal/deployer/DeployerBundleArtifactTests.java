package org.eclipse.virgo.kernel.model.internal.deployer;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.model.StubSpringContextAccessor;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.kernel.model.internal.SpringContextAccessor;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.region.StubRegion;
import org.eclipse.virgo.test.stubs.support.TrueFilter;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class DeployerBundleArtifactTests {

    private final StubBundleContext bundleContext = new StubBundleContext();
    {
        String filterString = String.format("(&(objectClass=%s)(artifactType=bundle))", DependencyDeterminer.class.getCanonicalName());
        bundleContext.addFilter(filterString, new TrueFilter(filterString));
    }

	private final StubRegion region = new StubRegion("test-region", null);

	private SpringContextAccessor springContextAccessor = new StubSpringContextAccessor();

	@Test(expected = FatalAssertionException.class)
	public void testNullBundleContext() {
		BundleInstallArtifact installArtifact = createMock(BundleInstallArtifact.class);
		new DeployerBundleArtifact(null, installArtifact, region, springContextAccessor);
	}

	@Test(expected = FatalAssertionException.class)
	public void testNullInstallArtifact() {
		new DeployerBundleArtifact(bundleContext, null, region, springContextAccessor);
	}
	
	@Test(expected = FatalAssertionException.class)
	public void testNullRegion() {
		BundleInstallArtifact installArtifact = createMock(BundleInstallArtifact.class);
		new DeployerBundleArtifact(bundleContext, installArtifact, null, springContextAccessor);
	}
	
	@Test(expected = FatalAssertionException.class)
	public void testNullSpringContextAccessor() {
		BundleInstallArtifact installArtifact = createMock(BundleInstallArtifact.class);
		new DeployerBundleArtifact(bundleContext, installArtifact, region, null);
	}

    @Test
    public void deleteEntry() throws DeploymentException {
		BundleInstallArtifact installArtifact = createMock(BundleInstallArtifact.class);

    	expect(installArtifact.getType()).andReturn("bundle");
    	expect(installArtifact.getName()).andReturn("test-bundle");
    	expect(installArtifact.getVersion()).andReturn(new Version("1.0.0"));
    	installArtifact.deleteEntry("foo");
    	replay(installArtifact);

    	DeployerBundleArtifact artifact = new DeployerBundleArtifact(bundleContext, installArtifact, region, springContextAccessor);
        artifact.deleteEntry("foo");

        verify(installArtifact);
    }

    @Test
    public void updateEntry() throws DeploymentException {
    	BundleInstallArtifact installArtifact = createMock(BundleInstallArtifact.class);
    	
    	expect(installArtifact.getType()).andReturn("bundle");
    	expect(installArtifact.getName()).andReturn("test-bundle");
    	expect(installArtifact.getVersion()).andReturn(new Version("1.0.0"));
    	installArtifact.updateEntry((URI) notNull(), eq("bar"));
    	replay(installArtifact);
    	
    	DeployerBundleArtifact artifact = new DeployerBundleArtifact(bundleContext, installArtifact, region, springContextAccessor);
    	artifact.updateEntry("foo", "bar");
    	
    	verify(installArtifact);
    }

    @Test
    public void getProperties() throws DeploymentException {
    	BundleInstallArtifact installArtifact = createMock(BundleInstallArtifact.class);

    	expect(installArtifact.getType()).andReturn("bundle");
    	expect(installArtifact.getName()).andReturn("test-bundle");
    	expect(installArtifact.getVersion()).andReturn(new Version("1.0.0"));
		Set<String> names = new HashSet<String>(Arrays.asList("foo", "bar", "deleted"));
    	expect(installArtifact.getPropertyNames()).andReturn(names);
    	expect(installArtifact.getProperty(eq("foo"))).andReturn("FOO");
    	expect(installArtifact.getProperty(eq("bar"))).andReturn("BAR");
		expect(installArtifact.getProperty(eq("deleted"))).andReturn(null);
		Bundle bundle = new StubBundle();
		expect(installArtifact.getBundle()).andReturn(bundle);

		SpringContextAccessor springContextAccessor = createMock(SpringContextAccessor.class);
		expect(springContextAccessor.isSpringPowered((Bundle) notNull())).andReturn(true);

		replay(installArtifact, springContextAccessor);

        DeployerBundleArtifact artifact = new DeployerBundleArtifact(bundleContext, installArtifact, region, springContextAccessor);
        Map<String, String> properties = artifact.getProperties();

        assertEquals("null values should be omitted.", 4, properties.size());
        assertEquals("FOO", properties.get("foo"));
        assertEquals("BAR", properties.get("bar"));

        assertEquals("" + bundle.getBundleId(), properties.get("Bundle Id"));
        assertEquals("true", properties.get("Spring"));

        verify(installArtifact, springContextAccessor);
    }

}
