package org.eclipse.virgo.nano.p2.build;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.equinox.p2.publisher.AbstractPublisherApplication;
import org.eclipse.equinox.p2.publisher.IPublisherAction;
import org.eclipse.equinox.p2.publisher.PublisherInfo;

public class BinaryPublisherApplication extends AbstractPublisherApplication {

	private File[] binaries = null;
	private Map<String, String> permissions = null;

	@Override
	protected IPublisherAction[] createActions() {
		ArrayList<IPublisherAction> result = new ArrayList<IPublisherAction>();
		if (binaries == null)
			binaries = new File[] {new File(source, "binary")}; //$NON-NLS-1$
		result.add(new VirgoBinariesAction(binaries, permissions));

		return result.toArray(new IPublisherAction[result.size()]);
	}
	
	@Override
	protected void processParameter(String arg, String parameter, PublisherInfo pinfo) throws URISyntaxException {
		super.processParameter(arg, parameter, pinfo);

		if (arg.equalsIgnoreCase("-chmod")) //$NON-NLS-1$
			permissions = parseChmodValues(parameter);
	}

    /**
     * Parses an array of mappings with syntax <b>location#permission</b>, where location :=
     * <b>targetFile@targetDir</b>. <b>targetFile</b> is the name of the file on which <b>permission</b> will be applied.
     * <b>targetDir</b> is the relative path from the root of the binary(zip).<\n> Ignores invalid mappings.
     * 
     * @param parameter - comma-separated list of location#permission values
     * @return - a map with all valid location and permission mappings
     */
    private Map<String, String> parseChmodValues(String parameter) {
        Map<String, String> result = new HashMap<String, String>();

        if (parameter != null && !parameter.isEmpty()) {
            String[] permissionsMapping = parameter.split(",");
            for (String mapping : permissionsMapping) {
                String[] splitMapping = mapping.split("#");
                if (splitMapping.length == 2) { // ignores invalid mappings
                    if (isLocationValid(splitMapping[0])) {
                        result.put(splitMapping[0], splitMapping[1]);
                    }
                }
            }
        }
        return result;
    }

    private boolean isLocationValid(String location) {
        String[] targetDetails = location.split("@");
        if (targetDetails != null && 
            targetDetails.length == 2 && 
            !targetDetails[0].isEmpty() && 
            !targetDetails[1].isEmpty()) {
            return true;
        }
        return false;
    }

}
