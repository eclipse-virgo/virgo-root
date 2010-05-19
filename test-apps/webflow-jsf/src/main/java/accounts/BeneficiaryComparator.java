package accounts;

import java.io.Serializable;
import java.util.Comparator;

public class BeneficiaryComparator implements Comparator<Beneficiary>, Serializable {

	public int compare(Beneficiary b1, Beneficiary b2) {
		return (b1.getName() == null || b2.getName() == null) ? -1 : b1.getName().compareTo(b2.getName());
	}

}
