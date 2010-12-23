
package org.eclipse.virgo.kernel.osgi.region;

import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.framework.StubServiceReference;
import org.eclipse.virgo.teststubs.osgi.framework.StubServiceRegistration;
import org.junit.Before;
import org.osgi.framework.ServiceReference;

public abstract class AbstractRegionServiceHookTest extends AbstractRegionHookTest {

    @SuppressWarnings("unchecked")
    private ServiceReference<String>[] serviceReferences = new StubServiceReference[3];

    public AbstractRegionServiceHookTest() {
        super();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void basicServiceSetUp() throws Exception {
        for (int i = 0; i < 3; i++) {
            StubServiceRegistration<String> reg = new StubServiceRegistration<String>((StubBundleContext) getBundle(i).getBundleContext(),
                "java.lang.String");
            serviceReferences[i] = new StubServiceReference<String>(reg);
        }
    }

    ServiceReference<String> getServiceReference(int index) {
        return this.serviceReferences[index];
    }

}