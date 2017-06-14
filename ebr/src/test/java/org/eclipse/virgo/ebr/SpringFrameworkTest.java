package org.eclipse.virgo.ebr;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
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
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.osgi.framework.Bundle.ACTIVE;

/**
 * Test class using PaxExam runner to test Spring Framework bundle resolution.
 * <p>
 * Created by dam on 6/9/17.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class SpringFrameworkTest {

    private static final String MIRROR_GROUP = "org.eclipse.virgo.mirrored";

    private static final String ASPECTJ_WEAVER = "org.aspectj.weaver";
    private static final String ASPECTJ_WEAVER_VERSION = "1.8.10";

    private static final String SF_VERSION = "4.3.9.RELEASE";
    private static final String SF_PREFIX = "org.springframework.";
    private static final String SF_CORE = SF_PREFIX + "core";
    private static final String SF_BEANS = SF_PREFIX + "beans";
    private static final String SF_AOP = SF_PREFIX + "aop";
    private static final String SF_ASPECTS = SF_PREFIX + "aspects";
    private static final String SF_EXPRESSION = SF_PREFIX + "expression";
    private static final String SF_CONTEXT = SF_PREFIX + "context";
    private static final String SF_CONTEXT_SUPPORT = SF_PREFIX + "context.support";
    private static final String SF_TRANSACTION = SF_PREFIX + "transaction";
    private static final String SF_JDBC = SF_PREFIX + "jdbc";
    private static final String SF_MESSAGING = SF_PREFIX + "messaging";
    private static final String SF_JMS = SF_PREFIX + "jms";
    private static final String SF_ORM = SF_PREFIX + "orm";
    private static final String SF_OXM = SF_PREFIX + "oxm";
    private static final String SF_WEB = SF_PREFIX + "web";
    private static final String SF_WEBMVC = SF_PREFIX + "webmvc";
    private static final String SF_WEBMVC_PORTLET = SF_PREFIX + "webmvc.portlet";
    private static final String SF_WEBSOCKET = SF_PREFIX + "websocket";

    @Inject
    private BundleContext bundleContext;

    @Configuration
    public Option[] config() {
        return options(
                // spring framework dependencies
                mavenBundle("commons-logging", "commons-logging", "1.2"),
                mavenBundle("javax.servlet", "javax.servlet-api", "3.1.0"),
                mavenBundle("javax.portlet", "portlet-api", "2.0"),
                bundle("http://build.eclipse.org/rt/virgo/ivy/bundles/release/org.eclipse.virgo.mirrored/oevm.org.aopalliance/1.0.0/oevm.org.aopalliance-1.0.0.jar"),
                bundle("http://build.eclipse.org/rt/virgo/ivy/bundles/release/org.eclipse.virgo.mirrored/javax.jms/1.1.0.v201205091237/javax.jms-1.1.0.v201205091237.jar"),
                // maven local
                mavenBundle(MIRROR_GROUP, ASPECTJ_WEAVER, ASPECTJ_WEAVER_VERSION),
                mavenBundle(MIRROR_GROUP, SF_CORE, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_BEANS, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_AOP, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_ASPECTS, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_EXPRESSION, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_CONTEXT, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_CONTEXT_SUPPORT, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_TRANSACTION, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_JDBC, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_MESSAGING, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_JMS, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_ORM, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_OXM, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_WEB, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_WEBMVC, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_WEBMVC_PORTLET, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_WEBSOCKET, SF_VERSION),
                // specify junit bundles
                junitBundles()
        );
    }

    @Test
    public void testAspectjWeaver() throws Exception {
        assertBundleActive(ASPECTJ_WEAVER);
    }

    @Test
    public void testSpringCore() throws Exception {
        assertBundleActive(SF_CORE);
    }

    @Test
    public void testSpringBeans() throws Exception {
        assertBundleActive(SF_BEANS);
    }

    @Test
    public void testSpringAop() throws Exception {
        assertBundleActive(SF_AOP);
    }

    @Test
    public void testSpringAspects() throws Exception {
        assertBundleActive(SF_ASPECTS);
    }

    @Test
    public void testSpringExpression() throws Exception {
        assertBundleActive(SF_EXPRESSION);
    }

    @Test
    public void testSpringContext() throws Exception {
        assertBundleActive(SF_CONTEXT);
    }

    @Test
    public void testSpringContextSupport() throws Exception {
        assertBundleActive(SF_CONTEXT_SUPPORT);
    }

    @Test
    public void testSpringTransaction() throws Exception {
        assertBundleActive(SF_TRANSACTION);
    }

    @Test
    public void testSpringJdbc() throws Exception {
        assertBundleActive(SF_JDBC);
    }

    @Test
    public void testSpringMessaging() throws Exception {
        assertBundleActive(SF_MESSAGING);
    }

    @Test
    public void testSpringJsm() throws Exception {
        assertBundleActive(SF_JMS);
    }

    @Test
    public void testSpringOrm() throws Exception {
        assertBundleActive(SF_ORM);
    }

    @Test
    public void testSpringOxm() throws Exception {
        assertBundleActive(SF_OXM);
    }

    @Test
    public void testSpringWeb() throws Exception {
        assertBundleActive(SF_WEB);
    }

    @Test
    public void testSpringWebMvc() throws Exception {
        assertBundleActive(SF_WEBMVC);
    }

    @Test
    public void testSpringWebMvcPortlet() throws Exception {
        assertBundleActive(SF_WEBMVC_PORTLET);
    }

    @Test
    public void testSpringWebsocket() throws Exception {
        assertBundleActive(SF_WEBSOCKET);
    }

    private void assertBundleActive(String symbolicName) throws BundleException {
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
