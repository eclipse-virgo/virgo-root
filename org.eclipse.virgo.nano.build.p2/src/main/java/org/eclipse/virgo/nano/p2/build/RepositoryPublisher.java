package org.eclipse.virgo.nano.p2.build;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.equinox.p2.publisher.AbstractPublisherApplication;
import org.eclipse.equinox.p2.publisher.eclipse.FeaturesAndBundlesPublisherApplication;
import org.osgi.service.component.ComponentContext;

public class RepositoryPublisher {

    private static final int SUCCESS = 0;
	private static final int ABNORMAL_EXITCODE = 666;
	private static final int REGULAR_PUBLISH = 1;
	private static final int BINARY_PUBLISH = 2;
	private static final String OPERATION_ID = "opId";
	private static final String[] INTERESTING_ARGS = {
		"-metadataRepository",
		"-artifactRepository",
		"-configs",
		"-source",
		"-publishArtifacts"
	};
	private static final String[] OPTIONAL_ARGS = {
	    "-append",
	    "-chmod"
	};
	
	public void activate(ComponentContext context) {
		int operation = Integer.valueOf(System.getProperty(OPERATION_ID));
		
		try {
			switch (operation) {
			case REGULAR_PUBLISH:
				executePublishingApp(new FeaturesAndBundlesPublisherApplication());
				System.exit(SUCCESS);
				break;
			case BINARY_PUBLISH:
				executePublishingApp(new BinaryPublisherApplication());
				System.exit(SUCCESS);
				break;
			default:
			    System.exit(SUCCESS);
			};
		} catch (Exception e) {
		    e.printStackTrace(System.err);
			System.exit(ABNORMAL_EXITCODE);
		}
	}

	private void executePublishingApp(AbstractPublisherApplication publishingApp) throws Exception {
		try {
			String[] args = readPublisherAppArguments();
			publishingApp.run(args);
		} catch (IllegalArgumentException iae) {
		    iae.printStackTrace(System.err);
			System.exit(ABNORMAL_EXITCODE);
		}
	}
	
	private String[] readPublisherAppArguments() {
		List<String> argsToProcess = new ArrayList<String>();
		
		for (String arg : INTERESTING_ARGS) {
            updateArgsToProcess(argsToProcess, arg, false);
        }
        for (String arg : OPTIONAL_ARGS) {
            updateArgsToProcess(argsToProcess, arg, true);
        }
        return argsToProcess.toArray(new String[argsToProcess.size()]);
	}

	private void updateArgsToProcess(List<String> argsToProcess, String arg, boolean areOptional) {
		String argParameter = System.getProperty(arg);
		if (argParameter != null) {
			argsToProcess.add(arg);
			if (!argParameter.isEmpty()) {
				argsToProcess.add(argParameter);
			}
		} else {
		    if (!areOptional) {
		        throw new IllegalArgumentException("Missing mandatory property: " + arg);
		    }
		}
	}

	public void deactivate(ComponentContext context) {
		//nothing to do
	}
}
