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

    private static final String SF_PREFIX = "org.springframework.";

    @Configuration
    @Override
    public Option[] config() {
        return options(
                // spring framework dependencies
                aspectJWeaver(),
                servletDependencies(),

                springframework("core"),
                springframework("beans"),
                springframework("aop"),
                springframework("aspects"),
                springframework("expression"),
                springframework("context"),
                springframework("context.support"),
                springframework("transaction"),
                springframework("jcl"),
                springframework("jdbc"),
                springframework("messaging"),
                springframework("jms"),
                springframework("orm"),
                springframework("oxm"),
                springframework("web"),
                springframework("webflux"),
                springframework("webmvc"),
                springframework("websocket"),
                // specify junit bundles
                junitBundles()
        );
    }

    @Test
    public void testSpringCore() {
        assertMirroredSpringframeworkBundleActive("core");
    }

    @Test
    public void testSpringBeans() {
        assertMirroredSpringframeworkBundleActive("beans");
    }

    @Test
    public void testSpringExpression() {
        assertMirroredSpringframeworkBundleActive("expression");
    }

    @Test
    public void testSpringAop() {
        assertMirroredSpringframeworkBundleActive("aop");
    }

    @Test
    public void testSpringAspects() {
        assertMirroredSpringframeworkBundleActive("aspects");
    }

    @Test
    public void testSpringContext() {
        assertMirroredSpringframeworkBundleActive("context");
    }

    @Test
    public void testSpringContextSupport() {
        assertMirroredSpringframeworkBundleActive("context.support");
    }

    @Test
    public void testSpringTransaction() {
        assertMirroredSpringframeworkBundleActive("transaction");
    }

    @Test
    public void testSpringJcl() {
        assertMirroredSpringframeworkBundleActive("jcl");
    }

    @Test
    public void testSpringJdbc() {
        assertMirroredSpringframeworkBundleActive("jdbc");
    }

    @Test
    public void testSpringMessaging() {
        assertMirroredSpringframeworkBundleActive("messaging");
    }

    @Test
    public void testSpringJms() {
        assertMirroredSpringframeworkBundleActive("jms");
    }

    @Test
    public void testSpringOrm() {
        assertMirroredSpringframeworkBundleActive("orm");
    }

    @Test
    public void testSpringOxm() {
        assertMirroredSpringframeworkBundleActive("oxm");
    }

    @Test
    public void testSpringWeb() {
        assertMirroredSpringframeworkBundleActive("web");
    }

    @Test
    public void testSpringWebflux() {
        assertMirroredSpringframeworkBundleActive("webflux");
    }

    @Test
    public void testSpringWebMvc() {
        assertMirroredSpringframeworkBundleActive("webmvc");
    }

    @Test
    public void testSpringWebsocket() {
        assertMirroredSpringframeworkBundleActive("websocket");
    }

    private void assertMirroredSpringframeworkBundleActive(String module) {
        assertMirroredBundleActive(SF_PREFIX + module);
    }

}
