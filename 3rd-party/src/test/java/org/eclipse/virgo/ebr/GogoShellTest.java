package org.eclipse.virgo.ebr;

import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.ops4j.pax.exam.CoreOptions.*;

public class GogoShellTest extends AbstractBaseTest {

    private static final String FELIX = "org.apache.felix";

    private static final String RUNTIME = "org.apache.felix.gogo.runtime";
    private static final String RUNTIME_VERSION_KEY = "gogoRuntimeVersion";

    private static final String SHELL = "org.apache.felix.gogo.shell";
    private static final String SHELL_VERSION_KEY = "gogoShellVersion";

    @Configuration
    @Override
    public Option[] config() {
        return options(
                // maven local
                mavenBundle(FELIX, RUNTIME, resolveVersionFromGradleProperties(RUNTIME_VERSION_KEY)),
                mavenBundle(FELIX, SHELL, resolveVersionFromGradleProperties(SHELL_VERSION_KEY)),
                // specify junit bundles
                junitBundles()
        );
    }

    @Test
    public void testGogoRuntime() {
        assertBundleActive(RUNTIME);
    }

    @Test
    public void testGogoShell() {
        assertBundleActive(SHELL);
    }
}
