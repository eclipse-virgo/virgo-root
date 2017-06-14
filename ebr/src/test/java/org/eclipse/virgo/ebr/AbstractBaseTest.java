package org.eclipse.virgo.ebr;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.inject.Inject;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeNotNull;
import static org.osgi.framework.Bundle.ACTIVE;

/**
 * Abstract test class to be extended by all test implementations.
 * <p>
 * Created by dam on 6/14/17.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public abstract class AbstractBaseTest {

    public static final String MIRROR_GROUP = "org.eclipse.virgo.mirrored";

    @Inject
    protected BundleContext bundleContext;

    public abstract Option[] config();

    protected void assertBundleActive(String symbolicName) throws BundleException {
        assumeNotNull(symbolicName);
        assumeFalse(symbolicName.isEmpty());
        for (Bundle b : this.bundleContext.getBundles()) {
            if (symbolicName.equals(b.getSymbolicName())) {
                if (ACTIVE != b.getState()) {
                    b.start(); // start the bundle so we get the exception
                }
                return;
            }
        }
        fail("Bundle with symbolicName [" + symbolicName + "] could not be found.");
    }
}
