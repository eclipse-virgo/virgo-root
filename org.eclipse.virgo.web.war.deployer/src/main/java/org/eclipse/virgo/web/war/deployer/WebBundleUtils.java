package org.eclipse.virgo.web.war.deployer;

import org.eclipse.gemini.web.internal.WebContainerUtils;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.osgi.framework.Constants;

public class WebBundleUtils {
	
    /**
     * Determines whether the given manifest represents a web application bundle. According to the R4.2 Enterprise
     * Specification, this is true if and only if the manifest contains any of the headers in Table 128.3:
     * Bundle-SymbolicName, Bundle-Version, Bundle-ManifestVersion, Import-Package, Web-ContextPath. Note: there is no
     * need to validate the manifest as if it is invalid it will cause an error later.
     * 
     * @param manifest the bundle manifest
     * @return <code>true</code> if and only if the given manifest represents a web application bundle
     */
    public static boolean isWebApplicationBundle(BundleManifest manifest) {
        return specifiesBundleSymbolicName(manifest) || specifiesBundleVersion(manifest) || specifiesBundleManifestVersion(manifest)
            || specifiesImportPackage(manifest) || specifiesWebContextPath(manifest);
    }
	
	/**
	 * Transforms the input string into a string with valid characters for a
	 * bundle symbolic name (valid characters are all alphanumerics plus '.',
	 * '_', '-'). All forbidden characters are replaced with '.'.
	 */
	public static String calculateCorrectSymbolicName(String originalName) {
		if (originalName != null) {
			return replaceForbiddenChars(originalName, '.');
		} else {
			return "";
		}
	}

	private static String replaceForbiddenChars(String st, char newChar) {
		char[] inpuStringChars = st.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (char currentChar : inpuStringChars) {
			if (isCharAllowedInSymbolicNames(currentChar)) {
				sb.append(currentChar);
			} else {
				sb.append(newChar);
			}
		}
		return sb.toString();
	}

	private static boolean isCharAllowedInSymbolicNames(char ch) {
		if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
				|| (ch >= '0' && ch <= '9') || ch == '_' || ch == '-'
				|| ch == '.') {
			return true;
		} else {
			return false;
		}
	}

    private static boolean specifiesBundleSymbolicName(BundleManifest manifest) {
        return manifest.getBundleSymbolicName().getSymbolicName() != null;
    }

    private static boolean specifiesBundleVersion(BundleManifest manifest) {
        return manifest.getHeader(Constants.BUNDLE_VERSION) != null;
    }

    private static boolean specifiesBundleManifestVersion(BundleManifest manifest) {
        return manifest.getBundleManifestVersion() != 1;
    }

    private static boolean specifiesImportPackage(BundleManifest manifest) {
        return !manifest.getImportPackage().getImportedPackages().isEmpty();
    }

    private static boolean specifiesWebContextPath(BundleManifest manifest) {
        return manifest.getHeader(WebContainerUtils.HEADER_WEB_CONTEXT_PATH) != null;
    }
}
