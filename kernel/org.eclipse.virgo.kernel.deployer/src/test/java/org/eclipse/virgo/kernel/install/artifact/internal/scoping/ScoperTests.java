
package org.eclipse.virgo.kernel.install.artifact.internal.scoping;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.virgo.kernel.install.artifact.internal.scoping.Scoper.DuplicateBundleSymbolicNameException;
import org.eclipse.virgo.kernel.install.artifact.internal.scoping.Scoper.DuplicateExportException;
import org.eclipse.virgo.kernel.install.artifact.internal.scoping.Scoper.UnsupportedBundleManifestVersionException;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.DynamicImportPackage;
import org.eclipse.virgo.util.osgi.manifest.DynamicallyImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.ExportPackage;
import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;
import org.eclipse.virgo.util.osgi.manifest.ImportPackage;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ScoperTests {

    private static final String SCOPE_NAME = "test_scope";

    private static BundleManifest manifest = null;

    private static BundleManifest unscopedManifest = null;

    private static List<BundleManifest> bundleManifests = new ArrayList<BundleManifest>();

    private static final File bundleFile = new File("src/test/resources/scoping/bundles/bug331767");

    private static final Object SCOPING_ATTRIBUTE_NAME = "module_scope";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        try (FileReader reader = new FileReader(new File(bundleFile, "META-INF/MANIFEST.MF"))) {
            manifest = BundleManifestFactory.createBundleManifest(reader);
            reader.close();
            unscopedManifest = BundleManifestFactory.createBundleManifest(manifest.toDictionary());
            bundleManifests.add(manifest);
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testScoping() throws UnsupportedBundleManifestVersionException, DuplicateExportException, DuplicateBundleSymbolicNameException {
        Scoper scoper = new Scoper(bundleManifests, SCOPE_NAME);
        scoper.scope();
        checkImports();
        checkDynamicImports();
        checkExports();
    }

    private void checkImports() {
        ImportPackage importPackage = manifest.getImportPackage();
        ImportPackage unscopedImportPackage = unscopedManifest.getImportPackage();
        List<ImportedPackage> ipList = importPackage.getImportedPackages();
        List<ImportedPackage> uipList = unscopedImportPackage.getImportedPackages();
        for (int i = 0; i < ipList.size(); i++) {
            Assert.assertTrue(ipList.get(i).getPackageName().equals(uipList.get(i).getPackageName()));
            Assert.assertTrue(ipList.get(i).getVersion().equals(uipList.get(i).getVersion()));
            for (String scopedAttribute : uipList.get(i).getAttributes().keySet()) {
                Assert.assertTrue(ipList.get(i).getAttributes().containsKey(scopedAttribute));
            }
            Assert.assertTrue(ipList.get(i).getAttributes().containsKey(SCOPING_ATTRIBUTE_NAME));
            Assert.assertTrue(uipList.get(i).getAttributes().size() + 1 == ipList.get(i).getAttributes().size());
        }
    }

    private void checkDynamicImports() {
        DynamicImportPackage dynamicImportPackage = manifest.getDynamicImportPackage();
        DynamicImportPackage unscopedDynamicImportPackage = unscopedManifest.getDynamicImportPackage();
        List<DynamicallyImportedPackage> dipList = dynamicImportPackage.getDynamicallyImportedPackages();
        List<DynamicallyImportedPackage> udipList = unscopedDynamicImportPackage.getDynamicallyImportedPackages();
        for (int i = 0; i < udipList.size(); i++) {
            Assert.assertTrue(dipList.get(i).getPackageName().equals(udipList.get(i).getPackageName()));
            Assert.assertTrue(dipList.get(i).getVersion().equals(udipList.get(i).getVersion()));
            for (String scopedAttribute : udipList.get(i).getAttributes().keySet()) {
                Assert.assertTrue(dipList.get(i).getAttributes().containsKey(scopedAttribute));
            }
            Assert.assertTrue(dipList.get(i).getAttributes().containsKey(SCOPING_ATTRIBUTE_NAME));
            Assert.assertTrue(udipList.get(i).getAttributes().size() + 1 == dipList.get(i).getAttributes().size());
        }
        Assert.assertTrue(dipList.size() == udipList.size() * 2);
    }

    private void checkExports() {
        ExportPackage exportPackage = manifest.getExportPackage();
        ExportPackage unscopedExportPackage = unscopedManifest.getExportPackage();
        List<ExportedPackage> epList = exportPackage.getExportedPackages();
        List<ExportedPackage> uepList = unscopedExportPackage.getExportedPackages();
        for (int i = 0; i < epList.size(); i++) {
            Assert.assertTrue(epList.get(i).getPackageName().equals(uepList.get(i).getPackageName()));
            Assert.assertTrue(epList.get(i).getVersion().equals(uepList.get(i).getVersion()));
            for (String scopedAttribute : uepList.get(i).getAttributes().keySet()) {
                Assert.assertTrue(epList.get(i).getAttributes().containsKey(scopedAttribute));
            }
            Assert.assertTrue(epList.get(i).getAttributes().containsKey(SCOPING_ATTRIBUTE_NAME));
            Assert.assertTrue(uepList.get(i).getAttributes().size() + 1 == epList.get(i).getAttributes().size());
        }
    }

}
