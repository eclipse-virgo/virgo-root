
package org.eclipse.virgo.kernel.osgi.region;

import java.util.ArrayList;
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
        for (int i = 0; i < 3; i++) {
            StubServiceRegistration<String> reg = new StubServiceRegistration<String>((StubBundleContext) getBundle(i).getBundleContext(),
                "java.lang.String");
            serviceReference[i] = new StubServiceReference<String>(reg);
        }
    }

    ServiceReference<String> getServiceReference(int index) {
        return this.serviceReference[index];
    }
    
    List<ServiceReference<String>> getServiceReferences() {
        List<ServiceReference<String>> l = new ArrayList<ServiceReference<String>>();
        for (ServiceReference<String> sr : this.serviceReference) {
            l.add(sr);
        }
        return l;
    }

}