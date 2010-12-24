
package org.eclipse.virgo.kernel.osgi.region;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.framework.StubServiceReference;
import org.eclipse.virgo.teststubs.osgi.framework.StubServiceRegistration;
import org.junit.Before;
import org.osgi.framework.ServiceReference;

public abstract class AbstractRegionServiceHookTest extends AbstractRegionHookTest {

    @SuppressWarnings("unchecked")
    private ServiceReference<String>[] serviceReference = new StubServiceReference[3];

    public AbstractRegionServiceHookTest() {
        super();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void basicServiceSetUp() throws Exception {
        for (int i = 0; i < NUM_BUNDLES; i++) {
            StubServiceRegistration<String> reg = new StubServiceRegistration<String>((StubBundleContext) getBundle(i).getBundleContext(),
                "java.lang.String");
            serviceReference[i] = new StubServiceReference<String>(reg);
        }
    }

    ServiceReference<String> getServiceReference(int index) {
        return this.serviceReference[index];
    }

    List<ServiceReference<?>> getServiceReferences() {
        List<ServiceReference<?>> l = new ArrayList<ServiceReference<?>>();
        for (ServiceReference<String> sr : this.serviceReference) {
            l.add(sr);
        }
        return l;
    }

    void assertServiceReferencePresent(Collection<ServiceReference<?>> serviceReferences, int... indices) {
        for (int i : indices) {
            assertTrue(serviceReferences.contains(getServiceReference(i)));
        }
    }

}