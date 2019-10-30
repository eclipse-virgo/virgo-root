package org.eclipse.virgo.ebr;

import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.ops4j.pax.exam.CoreOptions.*;

public class GeminiBlueprint3Test extends AbstractBaseTest {

    private static final String GEMINI_BLUEPRINT_VERSION = "3.0.0.M01";

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
                springframework("expression"),
                springframework("context"),

                mavenBundle("org.eclipse.gemini.blueprint", "gemini-blueprint-io", GEMINI_BLUEPRINT_VERSION),
                mavenBundle("org.eclipse.gemini.blueprint", "gemini-blueprint-core", GEMINI_BLUEPRINT_VERSION),
                mavenBundle("org.eclipse.gemini.blueprint", "gemini-blueprint-extender", GEMINI_BLUEPRINT_VERSION),

                // specify junit bundles
                junitBundles()
        );
    }

    @Test
    public void testGeminiBlueprintIo() {
        assertBundleActive("org.eclipse.gemini.blueprint.io");
    }

    @Test
    public void testGeminiBlueprintCore() {
        assertBundleActive("org.eclipse.gemini.blueprint.core");
    }

    @Test
    public void testGeminiBlueprintExtender() {
        assertBundleActive("org.eclipse.gemini.blueprint.extender");
    }
}
