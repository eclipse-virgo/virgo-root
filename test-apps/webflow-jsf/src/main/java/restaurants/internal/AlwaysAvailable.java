package restaurants.internal;

import common.money.MonetaryAmount;

import accounts.Account;
import restaurants.BenefitAvailabilityPolicy;

/**
 * A benefit availabilty policy that returns true at all times.
 */
public class AlwaysAvailable implements BenefitAvailabilityPolicy {
	static final BenefitAvailabilityPolicy INSTANCE = new AlwaysAvailable();

	public boolean isBenefitAvailableFor(Account account, MonetaryAmount diningAmount) {
		return true;
	}

	public String toString() {
		return "alwaysAvailable";
	}
}
