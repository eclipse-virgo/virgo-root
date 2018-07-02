package org.eclipse.virgo.ebr;

import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.ops4j.pax.exam.CoreOptions.*;

public class GeminiBlueprint3Test extends AbstractBaseTest {

    private static final String ASPECTJ_WEAVER = "org.aspectj.weaver";
    private static final String ASPECTJ_WEAVER_VERSION = "1.8.10";

    private static final String GEMINI_BLUEPRINT_VERSION = "3.0.0.M01";
    private static final String SF_VERSION = "5.0.8.RELEASE";

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
                mavenBundle(MIRROR_GROUP, ASPECTJ_WEAVER, ASPECTJ_WEAVER_VERSION),
                mavenBundle(MIRROR_GROUP, SF_CORE, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_BEANS, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_AOP, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_ASPECTS, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_EXPRESSION, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_CONTEXT, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_CONTEXT_SUPPORT, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_TRANSACTION, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_JCL, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_JDBC, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_MESSAGING, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_JMS, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_ORM, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_OXM, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_WEB, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_WEBFLUX, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_WEBMVC, SF_VERSION),
                mavenBundle(MIRROR_GROUP, SF_WEBSOCKET, SF_VERSION),

                mavenBundle("org.eclipse.gemini.blueprint", "gemini-blueprint-io", GEMINI_BLUEPRINT_VERSION),
                mavenBundle("org.eclipse.gemini.blueprint", "gemini-blueprint-core", GEMINI_BLUEPRINT_VERSION),
                mavenBundle("org.eclipse.gemini.blueprint", "gemini-blueprint-extender", GEMINI_BLUEPRINT_VERSION),

                // specify junit bundles
                junitBundles()
        );
    }

    @Test
    public void testGeminiBlueprintIo() throws Exception {
        assertBundleActive("org.eclipse.gemini.blueprint.io");
    }

    @Test
    public void testGeminiBlueprintCore() throws Exception {
        assertBundleActive("org.eclipse.gemini.blueprint.core");
    }

    @Test
    public void testGeminiBlueprintExtender() throws Exception {
        assertBundleActive("org.eclipse.gemini.blueprint.extender");
    }
}
