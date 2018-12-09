package org.eclipse.virgo.ebr;

import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * Test class testing Spring Framework bundle resolution.
 * <p>
 * Created by dam on 6/9/17.
 */
public class SpringFrameworkTest extends AbstractBaseTest {

    private static final String ASPECTJ_WEAVER_VERSION_KEY = "aspectjVersion";
    private static final String ASPECTJ_WEAVER = "org.aspectj.weaver";

    private static final String SF_VERSION_KEY = "springframeworkVersion";

    private static final String SF_PREFIX = "org.springframework.";
    private static final String SF_CORE = SF_PREFIX + "core";
    private static final String SF_BEANS = SF_PREFIX + "beans";
    private static final String SF_AOP = SF_PREFIX + "aop";
    private static final String SF_ASPECTS = SF_PREFIX + "aspects";
    private static final String SF_EXPRESSION = SF_PREFIX + "expression";
    private static final String SF_CONTEXT = SF_PREFIX + "context";
    private static final String SF_CONTEXT_SUPPORT = SF_PREFIX + "context.support";
    private static final String SF_TRANSACTION = SF_PREFIX + "transaction";
    private static final String SF_JCL = SF_PREFIX + "jcl";
    private static final String SF_JDBC = SF_PREFIX + "jdbc";
    private static final String SF_MESSAGING = SF_PREFIX + "messaging";
    private static final String SF_JMS = SF_PREFIX + "jms";
    private static final String SF_ORM = SF_PREFIX + "orm";
    private static final String SF_OXM = SF_PREFIX + "oxm";
    private static final String SF_WEB = SF_PREFIX + "web";
    private static final String SF_WEBFLUX = SF_PREFIX + "webflux";
    private static final String SF_WEBMVC = SF_PREFIX + "webmvc";
    private static final String SF_WEBSOCKET = SF_PREFIX + "websocket";

    @Configuration
    @Override
    public Option[] config() {
        return options(
                // spring framework dependencies
                mavenBundle("javax.servlet", "javax.servlet-api", "3.1.0"),
                mavenBundle("javax.portlet", "portlet-api", "2.0"),
                bundle("http://build.eclipse.org/rt/virgo/ivy/bundles/release/org.eclipse.virgo.mirrored/oevm.org.aopalliance/1.0.0/oevm.org.aopalliance-1.0.0.jar"),
                bundle("http://build.eclipse.org/rt/virgo/ivy/bundles/release/org.eclipse.virgo.mirrored/javax.jms/1.1.0.v201205091237/javax.jms-1.1.0.v201205091237.jar"),
                // maven local
                mavenBundle(MIRROR_GROUP, ASPECTJ_WEAVER, resolveVersionFromGradleProperties(ASPECTJ_WEAVER_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, SF_CORE, resolveVersionFromGradleProperties(SF_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, SF_BEANS, resolveVersionFromGradleProperties(SF_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, SF_AOP, resolveVersionFromGradleProperties(SF_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, SF_ASPECTS, resolveVersionFromGradleProperties(SF_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, SF_EXPRESSION, resolveVersionFromGradleProperties(SF_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, SF_CONTEXT, resolveVersionFromGradleProperties(SF_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, SF_CONTEXT_SUPPORT, resolveVersionFromGradleProperties(SF_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, SF_TRANSACTION, resolveVersionFromGradleProperties(SF_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, SF_JCL, resolveVersionFromGradleProperties(SF_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, SF_JDBC, resolveVersionFromGradleProperties(SF_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, SF_MESSAGING, resolveVersionFromGradleProperties(SF_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, SF_JMS, resolveVersionFromGradleProperties(SF_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, SF_ORM, resolveVersionFromGradleProperties(SF_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, SF_OXM, resolveVersionFromGradleProperties(SF_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, SF_WEB, resolveVersionFromGradleProperties(SF_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, SF_WEBFLUX, resolveVersionFromGradleProperties(SF_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, SF_WEBMVC, resolveVersionFromGradleProperties(SF_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, SF_WEBSOCKET, resolveVersionFromGradleProperties(SF_VERSION_KEY)),
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
    public void testSpringJcl() throws Exception {
        assertBundleActive(SF_JCL);
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
    public void testSpringWebflux() throws Exception {
        assertBundleActive(SF_WEBFLUX);
    }

    @Test
    public void testSpringWebsocket() throws Exception {
        assertBundleActive(SF_WEBSOCKET);
    }
}
