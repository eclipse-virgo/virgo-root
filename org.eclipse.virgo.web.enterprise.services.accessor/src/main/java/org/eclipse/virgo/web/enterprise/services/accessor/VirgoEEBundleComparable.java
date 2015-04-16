package org.eclipse.virgo.web.enterprise.services.accessor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.osgi.framework.Bundle;

public class VirgoEEBundleComparable implements Comparator<Bundle> {
	List<String> orderedImplBundles;
	
	public VirgoEEBundleComparable() {
		orderedImplBundles = new ArrayList<String>(WebAppBundleTrackerCustomizer.getBundles(System.getProperty(WebAppBundleTrackerCustomizer.IMPL_BUNDLES)).keySet());
	}

	@Override
	public int compare(Bundle b1, Bundle b2) {
		if(b1 == b2) {
			return 0;
		}
		
		String name1 = b1.getSymbolicName();
		String name2 = b2.getSymbolicName();
		
		Integer index1 = orderedImplBundles.indexOf(name1);
		Integer index2 = orderedImplBundles.indexOf(name2);
		
		if(index1 == -1 && index2 == -1) {
			return name1.compareTo(name2);
		} else if(index1 == -1) {
			return 1;
		} else if(index2 == -1) {
			return -1;
		} else { //normal int comparison
			return index1.compareTo(index2);
		}

	}

}
