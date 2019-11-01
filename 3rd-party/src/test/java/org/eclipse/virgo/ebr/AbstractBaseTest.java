package org.eclipse.virgo.ebr;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.internal.SelfDescribingValue;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeNotNull;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.streamBundle;
import static org.osgi.framework.Bundle.ACTIVE;
import static org.osgi.framework.namespace.PackageNamespace.PACKAGE_NAMESPACE;

/**
 * Abstract test class to be extended by all test implementations.
 * <p>
 * Created by dam on 6/14/17.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public abstract class AbstractBaseTest {

    private static final String SF_VERSION_KEY = "springframeworkVersion";

    private static final String ASPECTJ_WEAVER_VERSION_KEY = "aspectjVersion";
    private static final String JAVAX_EL_VERSION_KEY = "javaxElVersion";
    private static final String JAVAX_SERVLET_VERSION_KEY = "javaxServletVersion";
    private static final String JAVAX_SEVLET_JSP_VERSION_KEY = "javaxServletJspVersion";
    private static final String JAVAX_SEVLET_JSP_JSTL_VERSION_KEY = "javaxServletJspJstlVersion";

    @Inject
    private BundleContext bundleContext;

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public static UrlProvisionOption springframework(String module) {
        return getUrlProvisionOption("oevm.org.springframework." + module, SF_VERSION_KEY);
    }

    public abstract Option[] config();

    void assertMirroredBundleActive(String baseBundleName) {
        assertBundleActive("oevm." + baseBundleName);
    }

    void assertBundleActive(String symbolicName) {
        assumeNotNull(symbolicName);
        assumeFalse(symbolicName.isEmpty());
        for (Bundle b : this.bundleContext.getBundles()) {
            if (symbolicName.equals(b.getSymbolicName())) {
                if (ACTIVE != b.getState()) {
                    try {
                        b.start(); // start the bundle so we get the exception
                    } catch (BundleException e) {
                        e.printStackTrace();
                        fail("Failed to start bundle");
                    }
                }
                return;
            }
        }
        fail("Bundle with symbolicName [" + symbolicName + "] could not be found.");
    }

    static String resolveVersionFromGradleProperties(String libraryName) {
        String versionString = "unresolved";
        String gradlePropertiesFile = "../gradle.properties";
        try {
            Properties gradleProperties = new Properties();
            gradleProperties.load(new FileInputStream(gradlePropertiesFile));
            if (!gradleProperties.containsKey(libraryName)) {
                fail("Couldn't resolve '" + libraryName + "' in '" + gradlePropertiesFile + "'.");
            }
            return gradleProperties.getProperty(libraryName);
        } catch (IOException e) {
            fail("Failed to load '" + gradlePropertiesFile + " ' to get version for '" + libraryName + "'.");
        }
        return versionString;
    }

    static UrlProvisionOption getUrlProvisionOption(String bundleName, String key) {
        assertThat(Paths.get("build"), exists());
        assertThat(Paths.get("build", "plugins"), exists());
        String filename = bundleName + "_" + resolveVersionFromGradleProperties(key) + ".jar";
        Path bundlePath = Paths.get("build", "plugins", filename);
        assertThat(bundlePath, exists());

        try {
            return streamBundle(new FileInputStream(bundlePath.toFile()));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Failed to load test bundle", e);
        }
    }

    private static Matcher<Path> exists() {
        return new PathExistsMatcher();
    }

    private static class PathExistsMatcher extends BaseMatcher<Path> {
        @Override
        public boolean matches(Object actual) {
            if (actual instanceof Path) {
                return ((Path) actual).toFile().exists();
            }
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendDescriptionOf(new SelfDescribingValue<>("Path didn't exist."));
        }
    }

    Option aspectJWeaver() {
        return getUrlProvisionOption("oevm.org.aspectj.weaver", ASPECTJ_WEAVER_VERSION_KEY);
    }

    Option servletDependencies() {
            return new DefaultCompositeOption(
                    mavenBundle("javax.servlet", "javax.servlet-api", resolveVersionFromGradleProperties(JAVAX_SERVLET_VERSION_KEY)),
                    mavenBundle("javax.el", "javax.el-api", resolveVersionFromGradleProperties(JAVAX_EL_VERSION_KEY)),
                    mavenBundle("javax.servlet.jsp", "javax.servlet.jsp-api", resolveVersionFromGradleProperties(JAVAX_SEVLET_JSP_VERSION_KEY)),
                    mavenBundle("javax.servlet.jsp.jstl", "javax.servlet.jsp.jstl-api", resolveVersionFromGradleProperties(JAVAX_SEVLET_JSP_JSTL_VERSION_KEY))
            );
    }

    static Set<String> getExportedPackages(Bundle bundle) {
        Set<String> packagesExportedBySystemBundle = new HashSet<>(30);
        BundleWiring systemBundleWiring = bundle.adapt(BundleWiring.class);
        List<BundleCapability> packageWires = systemBundleWiring.getCapabilities(PACKAGE_NAMESPACE);
        for (BundleCapability packageWire : packageWires) {
            packagesExportedBySystemBundle.add((String) packageWire.getAttributes()
                    .get(PACKAGE_NAMESPACE));
        }
        return packagesExportedBySystemBundle;
    }

}
